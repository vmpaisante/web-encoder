var points = 0;
var points_text = "";
function updateOutputStatus(data) {
    var result = JSON.parse(data);
    $(".result").show();
    if (result.state == "finished") {
        $(".watch").show();
        $(".status").hide();
    }
    else if (result.state == "failed" ||
             result.state == "cancelled" ||
             result.state == "no_input" ||
             result.state == "skipped") {
      $(".status").html("Encode status: "+result.state);
      $(".goback").show();
    }
    else {
      $(".status").html("Encode status: "+result.state+points_text);
      points = (points+1)%4;
      if (points == 0) {
        points_text = "";
      } else {
        points_text = points_text + ".";
      }
    }
}

function checkEncodingStatus() {
	$.ajax({
        type: 'GET',
        url: "https://app.zencoder.com/api/v2/outputs/"+$(".output_id").text()+"/progress.json?api_key="+$(".zencoder_key").text(),
        dataType: 'text',
        success: updateOutputStatus
    });
}

$(document).ready(function(){
    // Start hiding model fields
    $(".input_id").hide();
    $(".output_id").hide();
    $(".output_url").hide();
    $(".zencoder_key").hide();
    $(".goback").hide();
    $(".watch").hide();

    checkEncodingStatus();
});

window.setInterval(function(){
  /// call your function here
  checkEncodingStatus();
}, 1000);
