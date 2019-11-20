###########################################
################## HTML ###################
###########################################

resource "google_storage_bucket_object" "index" {
  depends_on = ["google_storage_bucket.pacman"]
  bucket = data.template_file.storage_bucket_pacman.rendered
  name = "index.html"
  content_type = "text/html"
  source = "../../pacman/index.html"
}

resource "google_storage_object_acl" "index" {
  depends_on = ["google_storage_bucket_object.index"]
  bucket = data.template_file.storage_bucket_pacman.rendered
  object = "${google_storage_bucket_object.index.output_name}"
  role_entity = ["READER:allUsers"]
}

resource "google_storage_bucket_object" "error" {
  depends_on = ["google_storage_bucket.pacman"]
  bucket = data.template_file.storage_bucket_pacman.rendered
  name = "error.html"
  content_type = "text/html"
  source = "../../pacman/error.html"
}

resource "google_storage_object_acl" "error" {
  depends_on = ["google_storage_bucket_object.error"]
  bucket = data.template_file.storage_bucket_pacman.rendered
  object = "${google_storage_bucket_object.error.output_name}"
  role_entity = ["READER:allUsers"]
}

resource "google_storage_bucket_object" "start" {
  depends_on = ["google_storage_bucket.pacman"]
  bucket = data.template_file.storage_bucket_pacman.rendered
  name = "start.html"
  content_type = "text/html"
  source = "../../pacman/start.html"
}

resource "google_storage_object_acl" "start" {
  depends_on = ["google_storage_bucket_object.start"]
  bucket = data.template_file.storage_bucket_pacman.rendered
  object = "${google_storage_bucket_object.start.output_name}"
  role_entity = ["READER:allUsers"]
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

resource "google_storage_bucket_object" "css_files" {
  depends_on = ["google_storage_bucket.pacman"]
  count = length(var.css_files)
  bucket = data.template_file.storage_bucket_pacman.rendered
  name = "${var.css_files[count.index]}"
  content_type = "text/css"
  source = "../../pacman/${var.css_files[count.index]}"
}

resource "google_storage_object_acl" "css_files" {
  depends_on = ["google_storage_bucket_object.css_files"]
  count = length(var.css_files)
  bucket = data.template_file.storage_bucket_pacman.rendered
  object = "${var.css_files[count.index]}"
  role_entity = ["READER:allUsers"]
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

resource "google_storage_bucket_object" "img_files" {
  depends_on = ["google_storage_bucket.pacman"]
  count = length(var.img_files)
  bucket = data.template_file.storage_bucket_pacman.rendered
  name = "${var.img_files[count.index]}"
  content_type = "images/png"
  source = "../../pacman/${var.img_files[count.index]}"
}

resource "google_storage_object_acl" "img_files" {
  depends_on = ["google_storage_bucket_object.img_files"]
  count = length(var.img_files)
  bucket = data.template_file.storage_bucket_pacman.rendered
  object = "${var.img_files[count.index]}"
  role_entity = ["READER:allUsers"]
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

resource "google_storage_bucket_object" "js_files" {
  depends_on = ["google_storage_bucket.pacman"]
  count = length(var.js_files)
  bucket = data.template_file.storage_bucket_pacman.rendered
  name = "${var.js_files[count.index]}"
  content_type = "text/javascript"
  source = "../../pacman/${var.js_files[count.index]}"
}

resource "google_storage_object_acl" "js_files" {
  depends_on = ["google_storage_bucket_object.js_files"]
  count = length(var.js_files)
  bucket = data.template_file.storage_bucket_pacman.rendered
  object = "${var.js_files[count.index]}"
  role_entity = ["READER:allUsers"]
}

data "template_file" "game_js" {
  template = file("../../pacman/game/js/game.js")
  vars = {
    event_handler_api = join(",", formatlist("http://%s", google_compute_global_address.rest_proxy.*.address))
    cloud_provider = "GCP"
  }
}

resource "google_storage_bucket_object" "game_js" {
  depends_on = ["google_storage_bucket_object.js_files"]
  bucket = data.template_file.storage_bucket_pacman.rendered
  name = "game/js/game.js"
  content_type = "text/javascript"
  content = data.template_file.game_js.rendered
}

resource "google_storage_object_acl" "game_js" {
  depends_on = ["google_storage_bucket_object.game_js"]
  bucket = data.template_file.storage_bucket_pacman.rendered
  object = "${google_storage_bucket_object.game_js.output_name}"
  role_entity = ["READER:allUsers"]
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

resource "google_storage_bucket_object" "snd_files" {
  depends_on = ["google_storage_bucket.pacman"]
  count = length(var.snd_files)
  bucket = data.template_file.storage_bucket_pacman.rendered
  name = "${var.snd_files[count.index]}"
  content_type = "audio/mpeg"
  source = "../../pacman/${var.snd_files[count.index]}"
}

resource "google_storage_object_acl" "snd_files" {
  depends_on = ["google_storage_bucket_object.snd_files"]
  count = length(var.snd_files)
  bucket = data.template_file.storage_bucket_pacman.rendered
  object = "${var.snd_files[count.index]}"
  role_entity = ["READER:allUsers"]
}

###########################################
############### Pacman LBR ################
###########################################

resource "google_compute_global_address" "pacman" {
  name = "global-address-${var.global_prefix}"
}

resource "google_compute_global_forwarding_rule" "pacman" {
  name = "${var.global_prefix}-global-forwarding-rule"
  target = google_compute_target_http_proxy.pacman.self_link
  ip_address = google_compute_global_address.pacman.self_link
  port_range = "80"
}

resource "google_compute_target_http_proxy" "pacman" {
  name = "${var.global_prefix}-http-proxy"
  url_map = google_compute_url_map.pacman.self_link
}

resource "google_compute_url_map" "pacman" {
  name = "${var.global_prefix}-url-map"
  default_service = google_compute_backend_bucket.pacman.self_link
}

resource "google_compute_backend_bucket" "pacman" {
  name = "${var.global_prefix}-backend-bucket"
  bucket_name = "${google_storage_bucket.pacman.name}"
  enable_cdn = true
}
