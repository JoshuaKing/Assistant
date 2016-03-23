from = arguments[0];
return "{ 'result': '" + from + "'}";
$("#Form_FromAccountGuid option").each(function(i, opt) {
    if ($(opt).text().indexOf(from) >= 0) $(opt).prop("selected", true);
});
$("#Form_ToAccountGuid option").each(function(i, opt) {
    if ($(opt).text().indexOf(to) >= 0) $(opt).prop("selected", true);
});

$("#Form_FromDescription").val("AutoTopup <$" + min);
$("#SameAsFromAccount").click();
$("#Form_Amount").val(topup);
$("button.confirm-transfer").click();