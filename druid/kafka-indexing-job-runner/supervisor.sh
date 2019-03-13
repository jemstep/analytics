curl -X POST \
  http://localhost:8081/druid/indexer/v1/supervisor \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
  "type": "kafka",
  "dataSchema": {
    "dataSource": "jemstep-kafka-events",
    "parser": {
      "type": "avro_stream",
      "avroBytesDecoder": {
       "type" : "schema_registry",
      "url" : "http://localhost:8024",
      "capacity" : 100
      },
      "parseSpec": {
        "format": "avro",
        "timestampSpec": {
          "column": "timeStamp",
          "format": "auto"
        },
        "dimensionsSpec": {
          "dimensions": [
            "userId",
            "externalId",
            "organizationId",
            "userName",
            "email",
            "userSince",
            "billingState",
            "hasGoal",
            "host",
            "adClickId",
            "uuid",
        "bankName",
      "accountName",
      "ownership",
      "toAcctType",
      "fromBankName",
      "fromAccountRoutingNumber",
      "fromAcctType",
      "status",
      "updatedDate",
      "isEnrollment",
      "accountNumber",
      "fromAccountHolderName",
      "recordIdSeqNo",
      "standingInstructionRequestID",
      "oneOffAchsAmount",
      "isFutureDated"
          ]
        },
        "flattenSpec": {
    "useFieldDiscovery": true,
    "fields": [
      "timeStamp",
      {
        "type": "root",
        "name": "userId"
      },
      "externalId",
      "organizationId",
      {
        "type": "path",
        "name": "userName",
        "expr": "$.username.plainValue.value"
      },
      {
        "type": "path",
        "name": "email",
        "expr": "$.emailAddress.plainValue"
      },
      {
        "type": "path",
        "name": "userSince",
        "expr": "$.userStatus.userSince"
      },
      {
        "type": "path",
        "name": "billingState",
        "expr": "$.userStatus.billingState"
      },
      {
        "type": "path",
        "name": "hasGoal",
        "expr": "$.userStatus.userFlowStatus.hasGoal"
      },
      {
        "type": "path",
        "name": "host",
        "expr": "$.accessMeta.host"
      },
      {
        "type": "path",
        "name": "adClickId",
        "expr": "$.seoCampaign.adClickId"
      },
  "uuid",
    "bankName",
  "accountName",
  "ownership",
  "toAcctType",
  "fromBankName",
  "fromAccountRoutingNumber",
  "fromAcctType",
  "status",
  "updatedDate",
     {
        "type": "path",
        "name": "accountNumber",
        "expr": "$.accountNumber.plainValue"
      },
      {
        "type": "path",
        "name": "fromAccountHolderName",
        "expr": "$.fromAccountHolderName.plainValue"
      },      {
        "type": "path",
        "name": "recordIdSeqNo",
        "expr": "$.pershingSI.recordIdSequenceNo"
      },      {
        "type": "path",
        "name": "standingInstructionRequestID",
        "expr": "$.pershingSI.standingInstructionRequestID"
      },      {
        "type": "path",
        "name": "oneOffAchsAmount",
        "expr": "$.onceOffACHs[0].amount"
      },      {
        "type": "path",
        "name": "isFutureDated",
        "expr": "$.onceOffACHs[0].isFutureDated"
      }
    ]
  }
      }
    },
    "metricsSpec": [{
      "name": "count",
      "type": "count"
    },
    {
        "type": "doubleSum",
        "name": "totalOneOffAchsAmount",
        "fieldName": "oneOffAchsAmount"
      },
      {
  "type": "javascript",
  "name": "mostValuableCustomer",
  "fieldNames": ["oneOffAchsAmount"],
  "fnAggregate" : "function(current, oneOffAchsAmount)  { if (oneOffAchsAmount > 20000.00) { return current + 1; } else { return current; }}",
  "fnCombine"   : "function(partialA, partialB) { return partialA + partialB; }",
  "fnReset"     : "function()                   { return 0.0; }"
}
      ],
    "granularitySpec": {
      "type": "uniform",
      "segmentGranularity": "MINUTE",
      "queryGranularity": "NONE"
    }
  },
  "tuningConfig": {
    "type": "kafka",
    "maxRowsPerSegment": 5000000
  },
  "ioConfig": {
    "topic": "investor",
    "consumerProperties": {
      "bootstrap.servers": "localhost:29092"
    },
    "taskCount": 1,
    "replicas": 1,
    "taskDuration": "PT5M"
  }
}
'