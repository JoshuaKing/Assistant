var accounts = [];
$('.account-tile').each(function(i, div) {
    div = $(div);
    var balanceHuman = div.find('.balance dd.CurrentBalance').clone().children().remove().end().text();
    var account = div.find('.account-info h2').text();
    var type = div.parent().attr('data-analytics-productgroupname');
    var id = div.find('.account-info p').clone().children().remove().end().text().trim();
    var hashcode = 0;
    var str = id.replace(/[ ]+/g, '-');
    for (i = 0; i < str.length; i++) {
        c = str.charCodeAt(i);
        hashcode = c + (hashcode << 6) + (hashcode << 16) - hashcode;
    }
    hashcode = Math.abs(hashcode % 53);
    var balance = Number(balanceHuman.replace(/[$, ]/g, ''));
    accounts.push({
        'name': account.trim(),
        'balanceHuman': balanceHuman.trim(),
        'balance': balance,
        'type': type.toLowerCase().trim(),
        'id': id.trim(),
        'hashcode': hashcode
    });
});
return JSON.stringify(accounts);