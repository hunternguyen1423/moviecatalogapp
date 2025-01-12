function updateTotalPrice(totalPrice) {
    $("#total-amount").text(totalPrice.toFixed(2));
}

function getTotalPrice(resultData) {
    let sum = 0

    for (let i = 0; i < resultData.length; i++) {
        let total = resultData[i]["price"]*resultData[i]["quantity"];
        sum += total;
    }

    updateTotalPrice(sum);
}

$.ajax({
    url: 'api/shopping-cart',
    method: 'GET',
    dataType: 'json',
    success: function(data) {
        if (data[0]['id'] === "") {
            updateTotalPrice(0);
        } else {
            getTotalPrice(data);
        }
    },
    error: function() {
        alert("Cart is empty!");
    }
});

// Handle Place Order button click
document.getElementById("place-order-btn").addEventListener("click", function() {
    // Collect form data
    const firstName = document.getElementById("first-name").value;
    const lastName = document.getElementById("last-name").value;
    const cardNumber = document.getElementById("card-number").value;
    const expDate = document.getElementById("exp-date").value;

    $.ajax({
        url: "api/place-order?action=check",
        type: "POST",
        dataType: "json",
        data: {
            firstName: firstName,
            lastName: lastName,
            cardNumber: cardNumber,
            expDate: expDate
        },
        success: function(response) {
            window.location.href = "confirmation.html";
        },
        error: function(xhr) {
            alert("payment failed");
        }
    });
});