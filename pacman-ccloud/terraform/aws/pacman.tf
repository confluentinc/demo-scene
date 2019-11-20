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

###########################################
################### CSS ###################
###########################################

variable "css_files" {
  type = list(string)
  default = [
    "game/css/pacman-home.css",
    "game/css/pacman.css",
    "game/css/Quadrit.ttf",
  ]
}

resource "aws_s3_bucket_object" "css_files" {
  count = length(var.css_files)
  bucket = aws_s3_bucket.pacman.bucket
  key = var.css_files[count.index]
  content_type = "text/css"
  source = "../../pacman/${var.css_files[count.index]}"
}

###########################################
################### IMG ###################
###########################################

variable "img_files" {
  type = list(string)
  default = [
    "game/img/github.png",
    "game/img/move-down-big.png",
    "game/img/move-down.png",
    "game/img/move-left-big.png",
    "game/img/move-left.png",
    "game/img/move-right-big.png",
    "game/img/move-right.png",
    "game/img/move-up-big.png",
    "game/img/move-up.png",
    "game/img/sound-off.png",
    "game/img/sound-on.png",
    "game/img/pac-man-logo.png"
  ]
}

resource "aws_s3_bucket_object" "img_files" {
  count = length(var.img_files)
  bucket = aws_s3_bucket.pacman.bucket
  key = var.img_files[count.index]
  content_type = "images/png"
  source = "../../pacman/${var.img_files[count.index]}"
}

###########################################
################### JS ####################
###########################################

variable "js_files" {
  type = list(string)
  default = [
    "game/js/board.js",
    "game/js/bubbles.js",
    "game/js/fruits.js",
    "game/js/ghosts.js",
    "game/js/home.js",
    "game/js/jquery-buzz.js",
    "game/js/jquery-mobile.js",
    "game/js/jquery.js",
    "game/js/pacman.js",
    "game/js/paths.js",
    "game/js/sound.js",
    "game/js/tools.js"
  ]
}

resource "aws_s3_bucket_object" "js_files" {
  count = length(var.js_files)
  bucket = aws_s3_bucket.pacman.bucket
  key = var.js_files[count.index]
  content_type = "text/javascript"
  source = "../../pacman/${var.js_files[count.index]}"
}

data "template_file" "game_js" {
  template = file("../../pacman/game/js/game.js")
  vars = {
    event_handler_api = "${aws_api_gateway_deployment.event_handler_v1.invoke_url}${aws_api_gateway_resource.event_handler_resource.path}"
    cloud_provider = "AWS"
  }
}

resource "aws_s3_bucket_object" "game_js" {
  bucket = aws_s3_bucket.pacman.bucket
  key = "game/js/game.js"
  content_type = "text/javascript"
  content = data.template_file.game_js.rendered
}

###########################################
################# Sounds ##################
###########################################

variable "snd_files" {
  type = list(string)
  default = [
    "game/sound/die.mp3",
    "game/sound/eat-fruit.mp3",
    "game/sound/eat-ghost.mp3",
    "game/sound/eat-pill.mp3",
    "game/sound/eating.mp3",
    "game/sound/extra-life.mp3",
    "game/sound/ghost-eaten.mp3",
    "game/sound/ready.mp3",
    "game/sound/siren.mp3",
    "game/sound/waza.mp3"
  ]
}

resource "aws_s3_bucket_object" "snd_files" {
  count = length(var.snd_files)
  bucket = aws_s3_bucket.pacman.bucket
  key = var.snd_files[count.index]
  content_type = "audio/mpeg"
  source = "../../pacman/${var.snd_files[count.index]}"
}
