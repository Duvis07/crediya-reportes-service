#!/bin/bash

echo "Initializing AWS resources for REPORTES service in LocalStack..."

# Create DynamoDB Table for loan reports
echo "Creating DynamoDB table: loan-reports..."
awslocal dynamodb create-table \
    --table-name loan-reports \
    --attribute-definitions \
        AttributeName=id,AttributeType=S \
        AttributeName=reportType,AttributeType=S \
    --key-schema \
        AttributeName=id,KeyType=HASH \
        AttributeName=reportType,KeyType=RANGE \
    --billing-mode PAY_PER_REQUEST \
    --region us-east-1

echo "DynamoDB tables created successfully!"
echo "Note: SQS queue 'loan-approved-events-queue' is created by solicitudes-service init script"

# List created resources
echo "Available DynamoDB tables:"
awslocal dynamodb list-tables

echo "Available SQS queues:"
awslocal sqs list-queues

echo "AWS resources initialization for REPORTES completed!"
