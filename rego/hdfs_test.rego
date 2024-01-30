package authz
import rego.v1

test_hdfs if {
    allow with input as {
        "callerUgi": {
            "shortUserName": "HTTP"
        },
        "path": "/hosts",
        "operationName": "getfileinfo",
    }
}

test_hdfs_2 if {
    allow with input as {
        "callerUgi": {
            "shortUserName": "HTTP"
        },
        "path": "/hosts",
        "operationName": "delete",
    }
}

test_hdfs_3 if {
    allow with input as {
        "callerUgi": {
            "shortUserName": "HTTP"
        },
        "path": "/ro/nested/file",
        "operationName": "getfileinfo",
    }
}

test_hdfs_4 if {
    allow with input as {
        "callerUgi": {
            "shortUserName": "HTTP"
        },
        "path": "/ro/full/file",
        "operationName": "delete",
    }
}
