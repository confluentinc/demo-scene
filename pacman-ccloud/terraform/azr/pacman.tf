###########################################
################## HTML ###################
###########################################

resource "azurerm_storage_blob" "index" {
  depends_on = [module.staticweb]
  name = "index.html"
  content_type = "text/html"
  storage_account_name = azurerm_storage_account.pacman.name
  storage_container_name = "$web"
  type = "Block"
  source = "../../pacman/index.html"
}

resource "azurerm_storage_blob" "error" {
  depends_on = [module.staticweb]
  name = "error.html"
  content_type = "text/html"
  storage_account_name = azurerm_storage_account.pacman.name
  storage_container_name = "$web"
  type = "Block"
  source = "../../pacman/error.html"
}

resource "azurerm_storage_blob" "start" {
  depends_on = [module.staticweb]
  name = "start.html"
  content_type = "text/html"
  storage_account_name = azurerm_storage_account.pacman.name
  storage_container_name = "$web"
  type = "Block"
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

resource "azurerm_storage_blob" "css_files" {
  depends_on = [module.staticweb]
  count = length(var.css_files)
  name = var.css_files[count.index]
  storage_account_name = azurerm_storage_account.pacman.name
  storage_container_name = "$web"
  content_type = "text/css"
  type = "Block"
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

resource "azurerm_storage_blob" "img_files" {
  depends_on = [module.staticweb]
  count = length(var.img_files)
  name = var.img_files[count.index]
  storage_account_name = azurerm_storage_account.pacman.name
  storage_container_name = "$web"
  content_type = "images/png"
  type = "Block"
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

resource "azurerm_storage_blob" "js_files" {
  depends_on = [module.staticweb]
  count = length(var.js_files)
  name = var.js_files[count.index]
  storage_account_name = azurerm_storage_account.pacman.name
  storage_container_name = "$web"
  content_type = "text/javascript"
  type = "Block"
  source = "../../pacman/${var.js_files[count.index]}"
}

data "template_file" "game_js" {
  template = file("../../pacman/game/js/game.js")
  vars = {
    event_handler_api = "http://${azurerm_public_ip.rest_proxy[0].fqdn}"
    cloud_provider = "AZR"
  }
}

resource "azurerm_storage_blob" "game_js" {
  depends_on = [module.staticweb]
  name = "game/js/game.js"
  storage_account_name = azurerm_storage_account.pacman.name
  storage_container_name = "$web"
  content_type = "text/javascript"
  type = "Block"
  source_content = data.template_file.game_js.rendered
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

resource "azurerm_storage_blob" "snd_files" {
  depends_on = [module.staticweb]
  count = length(var.snd_files)
  name = var.snd_files[count.index]
  storage_account_name = azurerm_storage_account.pacman.name
  storage_container_name = "$web"
  content_type = "audio/mpeg"
  type = "Block"
  source = "../../pacman/${var.snd_files[count.index]}"
}
