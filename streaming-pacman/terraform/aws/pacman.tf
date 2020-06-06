###########################################
################## HTML ###################
###########################################

resource "aws_s3_bucket_object" "index" {
  bucket = aws_s3_bucket.pacman.bucket
  key = "index.html"
  content_type = "text/html"
  source = "../../pacman/index.html"
}

resource "aws_s3_bucket_object" "error" {
  bucket = aws_s3_bucket.pacman.bucket
  key = "error.html"
  content_type = "text/html"
  source = "../../pacman/error.html"
}

resource "aws_s3_bucket_object" "start" {
  bucket = aws_s3_bucket.pacman.bucket
  key = "start.html"
  content_type = "text/html"
  source = "../../pacman/start.html"
}

resource "aws_s3_bucket_object" "webmanifest" {
  bucket = aws_s3_bucket.pacman.bucket
  key = "site.webmanifest"
  content_type = "application/manifest+json"
  source = "../../pacman/site.webmanifest"
}

resource "aws_s3_bucket_object" "scoreboard" {
  bucket = aws_s3_bucket.pacman.bucket
  key = "scoreboard.html"
  content_type = "text/html"
  source = "../../pacman/scoreboard.html"
}

###########################################
################### CSS ###################
###########################################

resource "aws_s3_bucket_object" "css_files" {
  for_each = fileset(path.module, "../../pacman/game/css/*.*")
  bucket = aws_s3_bucket.pacman.bucket
  key = replace(each.key, "../../pacman/", "")
  content_type = "text/css"
  source = each.value
}

###########################################
################### IMG ###################
###########################################

resource "aws_s3_bucket_object" "img_files" {
  for_each = fileset(path.module, "../../pacman/game/img/*.*")
  bucket = aws_s3_bucket.pacman.bucket
  key = replace(each.key, "../../pacman/", "")
  content_type = "images/png"
  source = each.value
}

###########################################
################### JS ####################
###########################################

resource "aws_s3_bucket_object" "js_files" {
  for_each = fileset(path.module, "../../pacman/game/js/*.*")
  bucket = aws_s3_bucket.pacman.bucket
  key = replace(each.key, "../../pacman/", "")
  content_type = "text/javascript"
  source = each.value
}

data "template_file" "shared_js" {
  template = file("../../pacman/game/js/shared.js")
  vars = {
    cloud_provider = "AWS"
    event_handler_api = "${aws_api_gateway_deployment.event_handler_v1.invoke_url}${aws_api_gateway_resource.event_handler_resource.path}"
    ksqldb_query_api = "http://${aws_alb.ksqldb_lbr.dns_name}/query"
    scoreboard_api = "${aws_api_gateway_deployment.scoreboard_v1.invoke_url}${aws_api_gateway_resource.scoreboard_resource.path}"
  }
}

resource "aws_s3_bucket_object" "shared_js" {
  depends_on = [aws_s3_bucket_object.js_files]
  bucket = aws_s3_bucket.pacman.bucket
  key = "game/js/shared.js"
  content_type = "text/javascript"
  content = data.template_file.shared_js.rendered
}

###########################################
################# Sounds ##################
###########################################

resource "aws_s3_bucket_object" "snd_files" {
  for_each = fileset(path.module, "../../pacman/game/sound/*.*")
  bucket = aws_s3_bucket.pacman.bucket
  key = replace(each.key, "../../pacman/", "")
  content_type = "audio/mpeg"
  source = each.value
}
