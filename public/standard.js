function deleteActivity(obj) {
  var activity = $(obj.target).closest("article");
  var id = activity.attr("id");

  $.ajax({url: '/posts/' + id,
          type: 'DELETE',
          success: function (data) {
            console.log (data);
            activity.hide();
          },
          error: function (request, status) {
            console.log(request);
          }});
  return false;
}

function showCommentForm(obj) {
  var activity = $(obj.target).closest("article");
  var id = activity.attr("id");
  var jid = activity.find(".actor a").attr("content");
  var commentForm = activity.find(".comment-form");
  var url = '/posts/' + id + "/comment.template";

  var handleResponse = function(data){
    commentForm.html(data);
  };

  $.get(url, {jid: jid}, handleResponse);
  return false;
}

function shoutAuthor(obj) {
  var activity = $(obj.target).closest("article");

  // var id = activity.attr("id");
  var jid = activity.find(".actor a").attr("content");
  var recipientBox = $ (".recipient")
  recipientBox.each (
    function(index, element) {
      element.value ("");
      if (index == 0) {
        element.value (jid);
      } else {
        element.remove ();
      }
    }
  );

  return false;
}

function addRecipient (obj, ui) {
  var form = $ (obj.target).closest("form");
  // var recipientInput = form.find("#recipient-input").val();
  var recipientList = form.find(".recipient-list").first();
  var recipientLine = recipientList.find(".recipient-line").first();
  var lastLine = recipientList.find(".recipient-line").last();
  var newLine = recipientLine.clone();
  lastLine.after(newLine);
  newLine.removeClass("hidden");
  newLine.find(".recipient-name").text(ui.item.label);
  newLine.find("input").val(ui.item.value);
  return false;}


function removeRecipient (obj) {
  $ (obj.target).closest(".recipient-line").remove();
  return false;
}

function formatItem(data) {
  var splitData = data [0].split(",");
  return splitData[0];
}

function formatResult(data) {
  var splitData = data [0].split(",");
  return splitData[1];
}

$(function() {
  $ (".add-link").click(function (obj) {
    $(".link-section").show();
    return false;
  });
  $(".add-recipient")
    .live("click", addRecipient)
    .button({
      icons: {primary: "ui-icon-circle-plus"},
      text: false});
  $(".remove-recipient")
    .live("click", removeRecipient);
  $(".delete-activity")
    .live("click", deleteActivity);
  $(".comment")
    .live("click", showCommentForm);
  $(".recipient")
    .autocomplete({
      source: "/roster.js",
      minLength: 0,
      delay: 0,
      select: addRecipient});
});
