$(document).ready(function() {
    $("#go").click(function() {
        var coords = $("#coords").val();
        url = "rest/loadaggr?point=" + coords + "&direction=FORWARD"
        if ($("#now").prop('checked')) {
            url = "rest/load?point=" + coords + "&direction=FORWARD"
        }
        $.get(url, function(data) {
            $("#children tbody").remove();
            tdHTML = '<tbody>'
            $.each(data, function (i, item) {
                tdHTML += '<tr><td style="text-align:center">' + item.district +
                    '</td><td style="text-align:center">' + item.mo + '</td><td style="text-align:center">'
                    + Math.round(item.duration/60) + '</td></tr>'
            });
            tdHTML += '</tbody>'
            $('#children').append(tdHTML);
        })
    });
});