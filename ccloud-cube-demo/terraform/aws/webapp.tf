###########################################
################ S3 Bucket ################
###########################################

variable "bucket_suffix" {

    default = "<SET_YOUR_OWN_SUFFIX_WITHIN_THESE_DOUBLE_QUOTES>"

}

data "template_file" "cc_props_template" {

  template = "${file("templates/cc.props.tpl")}"

  vars {

    broker_list = "${var.ccloud_broker_list}"
    access_key = "${var.ccloud_access_key}"
    secret_key = "${var.ccloud_secret_key}"

  }

}

resource "aws_s3_bucket" "ccloud_demo" {

    bucket = "ccloud-demo-${var.bucket_suffix}"
    acl = "public-read"

    cors_rule {

        allowed_headers = ["*"]
        allowed_methods = ["GET", "POST"]
        allowed_origins = ["*"]

    }

    policy = <<EOF
{
    "Version": "2008-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::ccloud-demo${var.bucket_suffix}/*"
        }
    ]
}
    EOF

    website {

        index_document = "index.html"
        error_document = "error.html"

    }

    ###########################################
    ########### Local Provisioning ############
    ###########################################

    provisioner "local-exec" {

        command = "echo '${data.template_file.cc_props_template.rendered}' >> ~/.ccloud/config"
        interpreter = ["bash", "-c"]
        on_failure = "continue"

    }

    provisioner "local-exec" {

        command = "ccloud topic create _NUMBERS --partitions 4 --replication-factor 3"
        on_failure = "continue"

    }

    provisioner "local-exec" {

        command = "ccloud topic create _EVENTS --partitions 4 --replication-factor 3"
        on_failure = "continue"

    }

}

resource "aws_s3_bucket_object" "index" {

    bucket = "${aws_s3_bucket.ccloud_demo.bucket}"
    key = "index.html"
    content_type = "text/html"
    source = "../../webapp/index.html"
  
}

resource "aws_s3_bucket_object" "error" {

    bucket = "${aws_s3_bucket.ccloud_demo.bucket}"
    key = "error.html"
    content_type = "text/html"
    source = "../../webapp/error.html"
  
}

data "template_file" "play_content" {

  template = "${file("../../webapp/play.html")}"

  vars {

    rest_proxy_endpoint = "http://${aws_alb.rest_proxy.dns_name}"

  }

}

data "template_file" "cheat_content" {

  template = "${file("../../webapp/cheat.html")}"

  vars {

    rest_proxy_endpoint = "http://${aws_alb.rest_proxy.dns_name}"

  }

}

resource "aws_s3_bucket_object" "play" {

    bucket = "${aws_s3_bucket.ccloud_demo.bucket}"
    key = "play.html"
    content_type = "text/html"
    content = "${data.template_file.play_content.rendered}"
  
}

resource "aws_s3_bucket_object" "cheat" {

    bucket = "${aws_s3_bucket.ccloud_demo.bucket}"
    key = "cheat.html"
    content_type = "text/html"
    content = "${data.template_file.cheat_content.rendered}"
  
}

resource "aws_s3_bucket_object" "ccloud_logo" {

    bucket = "${aws_s3_bucket.ccloud_demo.bucket}"
    key = "ccloud-logo.jpg"
    source = "../../webapp/ccloud-logo.jpg"
  
}

output "1) Playing the Game          " {

    value = "http://${aws_s3_bucket.ccloud_demo.website_endpoint}"

}