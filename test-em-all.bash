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

function waitForService() {
    url="$@"
    echo -n "Wait for: $url..."
    n=0
    until testUrl "$url"
    do
      n=$(( n + 1 ))
      if (( n == 100 ))
      then
        echo "Give up"
        exit 1
      else
        sleep 3
        echo -n ", retry #$n "
      fi
    done
    echo "DONE, continues..."
}

function testUrl() {
    url="$@"
    if curl  "$url" -k -s -f -o /dev/null
    then
      return 0
    else
      return 1
    fi
}

function assertCurl() {
    local expectedHttpCode=$1
    local curlCmd="$2 -w \"%{http_code}\""
    local result=$(eval "$curlCmd")
    local httpCode="${result:(-3)}"
    RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

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
    else
      echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    fi
}

function waitForMessageProcessing() {
  echo "Wait for message to be processed... "

  # Give background processing time to complete
  sleep 1

  n=0
  until testCompositeCreated
  do
    n=$(( n + 1 ))
    if (( n == 50 ))
    then
      echo " Give up"
      exit 1
    else
      sleep 6
      echo -n ", retry #$n"
    fi
  done
  echo "All messages are now processed"
}

function setupTestData() {
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
}

function recreateComposite() {
  local productId="$1"
  local composite="$2"

  assertCurl 202 "curl -X DELETE -H \"$AUTH_HEADER\" -k -s https://$HOST:$PORT/product-composite/$productId"

  local response
  response=$(curl -X POST -k -s https://"$HOST":"$PORT"/product-composite \
    -H "Content-Type: application/json" -H "$AUTH_HEADER" \
    -d "$composite" -w "%{http_code}")
  assertEqual 202 "$response"
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
echo "Checking service health at: https://$HOST:$PORT/actuator/health..."
waitForService "https://$HOST:$PORT/actuator/health"

# Verify access to the Eureka and that all 6 microservices are registered; gateway, auth server, review, product, recommendation, product composite
echo "Verifying Eureka access and registration of all 6 services..."
assertCurl 200 "curl -H \"accept:application/json\" -k https://user:pwd@$HOST:$PORT/eureka/api/apps -s"
serviceCount=$(echo "$RESPONSE" | jq ".applications.application | length")
assertEqual 6 "$serviceCount"

echo "Fetching access token..."
ACCESS_TOKEN=$(curl -k -s "https://writer:secret-writer@$HOST:$PORT/oauth2/token" \
  -d "grant_type=client_credentials" -d "scope=product:read product:write" | jq .access_token -r)
echo "Access token: $ACCESS_TOKEN"
AUTH_HEADER="Authorization: Bearer $ACCESS_TOKEN"

echo "Setting up test data..."
setupTestData

if [[ $@ == *"stop"* ]]
then
  echo "We are done, stopping the test environment..."
  echo "$ docker compose down"
  docker compose down
fi

echo "End, all tests OK: $(date)"