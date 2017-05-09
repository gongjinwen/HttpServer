$(document).ready(function() {
  var name_items = $('.name-item');
  name_items.click(function(event) {
    var name = $(this).text();
    $('#device-name').val(name);
    $("#device-name-group").removeClass("has-error");
    event.preventDefault();
  });

  $('#network').change(function() {
    var network = $('#network').find(':selected');
    var password_group = $('#password-group');
    
    $("#network-group").removeClass("has-error");

    if (network.data("password") !== undefined) {
      password_group.removeClass("hidden");
    } else {
      password_group.addClass("hidden");
      $("#password").val("");
    }
  });

  $('#connect').click(function(event) {
    var is_valid = true;
    var device_name_group = $("#device-name-group");
    var network_select = $("#network");

    var device_name = $("#device-name").val().trim();
    if (device_name === "") {
      is_valid = false;
      device_name_group.addClass("has-error");
      console.log("add class");
    } else {
      device_name_group.removeClass("has-error");
    }

    var network = network_select.find(":selected");
    var network_group = $("#network-group");
    if (network.prop("index") == 0) {
      is_valid = false;
      network_group.addClass("has-error");
    } else {
      network_group.removeClass("has-error");
    }

    if (network.data("password") !== undefined) {
      var password = $('#password').val();
      var password_group = $("#password-group");
      console.log(password);
      if (password.length < 8) {
        is_valid = false;
        password_group.addClass("has-error");
      } else {
        password_group.removeClass("has-error");
      }
    }

    if (!is_valid) {
      event.preventDefault();
    }
  });

});
