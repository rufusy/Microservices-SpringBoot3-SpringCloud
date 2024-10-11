#!/bin/bash
#
# Sample usage:
#
# ./test-em-all.bash start
# ./test-em-all.bash start stop

: ${HOST=localhost}
: ${PORT=8443}
: ${PROD_ID_REVS_RECS=1} # product with recommendations and reviews
: ${PROD_ID_NOT_FOUND=13} # missing product
: ${PROD_ID_NO_RECS=113} # product with no recommendations
: ${PROD_ID_NO_REVS=213} # product with no reviews

function testUrl() {
    url="$@"
    if curl "$url" -k -s -f -o /dev/null
    then
      return 0
    else
      return 1
    fi
}

function waitForService() {
    local retryLimit=${2:-50}
    local url="$1"
    
    echo -n "Wait for: $url..."
    
    n=0
    until testUrl "$url"; do
      n=$(( n + 1 ))
      if (( n == retryLimit )); then
        echo "Give up after $retryLimit retries."
        exit 1
      else
        sleep 3
        echo -n ", retry #$n "
      fi
    done
    echo "DONE, continues..."
}

function assertCurl() {
    local expectedHttpCode=$1
    local curlCmd="$2 -w \"%{http_code}\""
    local result=$(eval "$curlCmd")
    local httpCode="${result:(-3)}"
    
    if (( ${#result} > 3 )); then
      RESPONSE="${result%???}"
    else
      RESPONSE=''
    fi

    if [ "$httpCode" = "$expectedHttpCode" ]
    then
      if [ "$httpCode" = "200" ]
      then
        echo "Test OK (HTTP code: $httpCode)"
      else
        echo "Test OK (HTTP code: $httpCode, $RESPONSE)"
      fi
    else
      echo "Test FAILED, EXPECTED HTTP code: $expectedHttpCode, GOT: $httpCode, WILL ABORT"
      echo "- Failing command: $curlCmd"
      echo "- Response body: $RESPONSE"
      exit 1
    fi
}

function assertEqual() {
    local expected=$1
    local actual=$2

    if [ "$actual" = "$expected" ]
    then
      echo "Test OK (actual value: $actual)"
      return 0
    else
      echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
      return 1
    fi
}

function setupTestData() {
  body="{\"productId\":$PROD_ID_NO_RECS}"
  body+=',"name":"product name A", "weight":100, "reviews":[
    {"reviewId":1,"author":"author 1","subject":"subject 1", "content":"content 1"},
    {"reviewId":2,"author":"author 2","subject":"subject 2", "content":"content 2"},
    {"reviewId":3,"author":"author 3","subject":"subject 3", "content":"content 3"}
  ]}'
  recreateComposite "$PROD_ID_NO_RECS" "$body"
 
  body="{\"productId\":$PROD_ID_NO_REVS"
  body+=',"name":"product name B","weight":200, "recommendations":[
    {"recommendationId":1,"author":"author 1","rate":1,"content":"content 1"},
    {"recommendationId":2,"author":"author 2","rate":2,"content":"content 2"},
    {"recommendationId":3,"author":"author 3","rate":3,"content":"content 3"}
  ]}'
  recreateComposite "$PROD_ID_NO_REVS" "$body"
  
  body="{\"productId\":$PROD_ID_REVS_RECS"
  body+=',"name":"product name C","weight":300,
    "recommendations":[
      {"recommendationId":1,"author":"author 1","rate":1,"content":"content 1"},
      {"recommendationId":2,"author":"author 2","rate":2,"content":"content 2"},
      {"recommendationId":3,"author":"author 3","rate":3,"content":"content 3"}
    ],
    "reviews":[
      {"reviewId":1,"author":"author 1","subject":"subject 1","content":"content 1"},
      {"reviewId":2,"author":"author 2","subject":"subject 2","content":"content 2"},
      {"reviewId":3,"author":"author 3","subject":"subject 3","content":"content 3"}
    ]
  }'
  recreateComposite "$PROD_ID_REVS_RECS" "$body"
}

function recreateComposite() {
  local productId="$1"
  local composite="$2"

  assertCurl 202 "curl -X DELETE -H \"$AUTH_HEADER\" -k -s https://\"$HOST\":\"$PORT\"/product-composite/$productId"

  local response
  response=$(curl -X POST -k -s https://"$HOST":"$PORT"/product-composite \
    -H "Content-Type: application/json" -H "$AUTH_HEADER" \
    -d "$composite" -w "%{http_code}")
  assertEqual 202 "$response"
}

function testCompositeCreated() {

  # Expect that the Product Composite for productId PROD_ID_REVS_RECS has been created with three recommendations and three reviews
  if ! assertCurl 200 "curl -H \"$AUTH_HEADER\" -k -s https://$HOST:$PORT/product-composite/$PROD_ID_REVS_RECS"; then
    echo -n " FAIL"
    return 1
  fi
  
  set +e
  
  assertEqual "$PROD_ID_REVS_RECS" "$(echo $RESPONSE | jq .productId)"
  if [ $? -eq 1 ]; then return 1; fi
  
  assertEqual 3 "$(echo $RESPONSE | jq '.recommendations | length')"
  if [ $? -eq 1 ]; then return 1; fi
  
  assertEqual 3 "$(echo $RESPONSE | jq '.reviews | length')"
  if [ $? -eq 1 ]; then return 1; fi
  
  set -e
}

function waitForMessageProcessing() {
  local retryLimit=${1:-50}
  
  sleep 1
  
  n=0
  until testCompositeCreated
  do
    n=$(( n + 1 ))
    if (( n == retryLimit )); then
      echo " Give up after $retryLimit retries."
      exit 1
    else
      sleep 6
      echo -n ", retry #$n "
    fi
  done
  
  echo "All messages are now processed after $n retries."
}


set -e

echo "Start Tests: $(date)"

echo "HOST={$HOST}"
echo "PORT={$PORT}"

if [[ $@ = *"start"* ]]
then
  echo "Restarting the test environment..."
  echo "$ docker compose down --remove-orphans"
  docker compose down --remove-orphans
  echo "$ docker compose up -d"
  docker compose up -d
fi

# Check services health
echo "Check service health at: https://$HOST:$PORT/actuator/health..."
waitForService "https://$HOST:$PORT/actuator/health" 60

# Verify access to the Eureka and that all 6 microservices are registered; gateway, auth server, review, product, recommendation, product composite
echo "Verify Eureka access and registration of all 6 services..."
assertCurl 200 "curl -H \"accept:application/json\" -k https://user:pwd@$HOST:$PORT/eureka/api/apps -s"
serviceCount=$(echo "$RESPONSE" | jq ".applications.application | length")
assertEqual 6 "$serviceCount"

echo "Fetch access token..."
ACCESS_TOKEN=$(curl -k -s "https://writer:secret-writer@$HOST:$PORT/oauth2/token" \
  -d "grant_type=client_credentials" -d "scope=product:read product:write" | jq .access_token -r)
echo "Access token: $ACCESS_TOKEN"
AUTH_HEADER="Authorization: Bearer $ACCESS_TOKEN"
AUTH="-H \"Authorization: Bearer $ACCESS_TOKEN\""

echo "Set up test data..."
setupTestData

echo "Wait for messages to be processed..."
waitForMessageProcessing 60

# Verify that a normal request works, expect three recommendations and three reviews
assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/product-composite/$PROD_ID_REVS_RECS -s"
assertEqual $PROD_ID_REVS_RECS $(echo $RESPONSE | jq .productId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")

# Verify that a 404 (Not Found) error is returned for a non-existing productId ($PROD_ID_NOT_FOUND)
assertCurl 404 "curl $AUTH -k https://$HOST:$PORT/product-composite/$PROD_ID_NOT_FOUND -s"
assertEqual "No product found for productId: $PROD_ID_NOT_FOUND" "$(echo $RESPONSE | jq -r .message)"

# Verify that no recommendations are returned for productId $PROD_ID_NO_RECS
assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/product-composite/$PROD_ID_NO_RECS -s"
assertEqual $PROD_ID_NO_RECS $(echo $RESPONSE | jq .productId)
assertEqual 0 $(echo $RESPONSE | jq ".recommendations | length")

# Verify that no reviews are returned for productId $PROD_ID_NO_REVS
assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/product-composite/$PROD_ID_NO_REVS -s"
assertEqual $PROD_ID_NO_REVS $(echo $RESPONSE | jq .productId)
assertEqual 0 $(echo $RESPONSE | jq ".reviews | length")

# Verify that a 422 (Unprocessable Entity) error is returned for a productId that is out of range (-1)
assertCurl 422 "curl $AUTH -k https://$HOST:$PORT/product-composite/-1 -s"
assertEqual "\"Invalid productId: -1\"" "$(echo $RESPONSE | jq .message)"

# Verify that a 400 (Bad Request) error error is returned for a productId that is not a number, i.e. invalid format
assertCurl 400 "curl $AUTH -k https://$HOST:$PORT/product-composite/invalidProductId -s"
assertEqual "\"Type mismatch.\"" "$(echo $RESPONSE | jq .message)"

# Verify that a request without access token fails on 401, Unauthorized
assertCurl 401 "curl -k https://$HOST:$PORT/product-composite/$PROD_ID_REVS_RECS -s"

# Verify that the reader - client with only read scope can call the read API but not delete API.
READER_ACCESS_TOKEN=$(curl -k https://reader:secret-reader@$HOST:$PORT/oauth2/token -d grant_type=client_credentials -d scope="product:read" -s | jq .access_token -r)
echo READER_ACCESS_TOKEN=$READER_ACCESS_TOKEN
READER_AUTH="-H \"Authorization: Bearer $READER_ACCESS_TOKEN\""

assertCurl 200 "curl $READER_AUTH -k https://$HOST:$PORT/product-composite/$PROD_ID_REVS_RECS -s"
assertCurl 403 "curl -X DELETE $READER_AUTH -k https://$HOST:$PORT/product-composite/$PROD_ID_REVS_RECS -s"

# Verify access to Swagger and OpenAPI URLs
echo "Swagger/OpenAPI tests"
assertCurl 302 "curl -ks  https://$HOST:$PORT/openapi/swagger-ui.html"
assertCurl 200 "curl -ksL https://$HOST:$PORT/openapi/swagger-ui.html"
assertCurl 200 "curl -ks  https://$HOST:$PORT/openapi/webjars/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config"
assertCurl 200 "curl -ks  https://$HOST:$PORT/openapi/v3/api-docs"
assertEqual "3.0.1" "$(echo $RESPONSE | jq -r .openapi)"
assertEqual "https://$HOST:$PORT" "$(echo $RESPONSE | jq -r '.servers[0].url')"
assertCurl 200 "curl -ks  https://$HOST:$PORT/openapi/v3/api-docs.yaml"


if [[ $@ == *"stop"* ]]
then
  echo "We are done, stopping the test environment..."
  echo "$ docker compose down"
  docker compose down
fi

echo "End, all tests OK: $(date)"
