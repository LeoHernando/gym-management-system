#!/bin/bash

set -e # Stops script if any command fails

aws --endpoint-url=http://localhost:4566 cloudformation deploy \
    --stack-name gym-management \
    --template-file "./cdk.out/localstack.template.json"

aws --endpoint-url=http://localhost:4566 elbv2 describe-load-balancers \
    --query "LoadBalancers[0].DNSName" --output text
