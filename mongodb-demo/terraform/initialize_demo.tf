resource "null_resource" "init_demo" {

// Copy init_demo script to the VM
  provisioner "file" {
    source      = "${path.module}/init_demo.sh"
    destination = "/tmp/init_demo.sh"

    connection {
      user     = var.ssh_user
      password = var.ssh_password
      insecure = true
      host     = var.vm_host
    }
  }

  provisioner "file" {
    content      = templatefile("${path.module}/deploy_docs.tpl", { 
      ext_ip = var.vm_host
    })
    destination = "/tmp/deploy_docs.sh"

    connection {
      user     = var.ssh_user
      password = var.ssh_password
      insecure = true
      host     = var.vm_host
    }
  }

  // Copy docs to the VM
  provisioner "file" {
    source      = "../asciidoc/"
    destination = ".workshop/docker/asciidoc"

    connection {
      user     = var.ssh_user
      password = var.ssh_password
      insecure = true
      host     = var.vm_host
    }
  }

  // Execute deploy_docs script on the VM to deploy docs
  provisioner "remote-exec" {
    inline = [
      "sleep 30",
      "chmod +x /tmp/deploy_docs.sh",
      "/tmp/deploy_docs.sh"
    ]

    connection {
      user     = var.ssh_user
      password = var.ssh_password
      insecure = true
      host     = var.vm_host
    }
  }

  // Execute init_demo script on the VM to initialize the demo
  provisioner "remote-exec" {
    inline = [
      "sleep 30",
      "chmod +x /tmp/init_demo.sh",
      "/tmp/init_demo.sh"
    ]

    connection {
      user     = var.ssh_user
      password = var.ssh_password
      insecure = true
      host     = var.vm_host
    }
  }

}

variable "ssh_user" {
  description = "SSH Username to connect to the VM"
}

variable "ssh_password" {
  description = "SSH password to connect to the VM"
}

variable "vm_host" {
  description = "VM HOST , will be used to ssh"
}




 