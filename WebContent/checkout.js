function getCart() {
    $.ajax({
        url: 'api/shopping-cart',
        method: 'GET',
        dataType: 'json',
        success: function(data) {
            if (data[0]['id'] === "") {
                let cartItemsContainer = jQuery("#cart_table_body");
                let title = jQuery("#page-name");
                cartItemsContainer.empty();
                title.append("<p>erm ur cart is empty. pls add sumting</p>")
                updateTotalPrice(0);
            } else {
                displayCartItems(data);
            }
        },
        error: function() {
            alert("Cart is empty!");
        }
    });
}

$(document).ready(function() {
    getCart();
});

function updateTotalPrice(totalPrice) {
    $("#total-price").text(totalPrice.toFixed(2));
}

function displayCartItems(resultData) {
    let cartItemsContainer = jQuery("#cart_table_body");
    cartItemsContainer.empty();
    let sum = 0

    for (let i = 0; i < resultData.length; i++) {
        let total = resultData[i]["price"]*resultData[i]["quantity"];
        sum += total;

        let rowHTML = "<tr>";
        rowHTML += "<th><a href='single-movie.html?id=" + resultData[i]["id"] + "'>" + resultData[i]["title"] + "</a></th>";
        rowHTML += "<th class='cart-quantity-header'>"
            + "<input type='number' id='cart-quantity-" + resultData[i]["id"] + "' "
            + "class='cart-quantity-input' value='" + resultData[i]['quantity'] + "' min='1'>"
            + "<button class='update-quantity-btn' data-movie-id='" + resultData[i]["id"] + "'>↻</button>"
            + "</th>";
        rowHTML += "<th><button class='remove-from-cart-btn' data-movie-id='"
            + resultData[i]["id"]
            + "'>-</button></th>";
        rowHTML += "<th>$" + resultData[i]["price"] + "</th>";
        rowHTML += "<th>$" + total + "</th>";

        rowHTML += "</tr>";

        cartItemsContainer.append(rowHTML);
    }

    updateTotalPrice(sum);
}

$(document).on('click', '.update-quantity-btn', function() {
    let movieId = $(this).data('movie-id');
    let quantity = $('#cart-quantity-' + movieId).val();

    $.ajax({
        dataType: "json",
        method: "POST",
        url: "api/shopping-cart?action=updateCart",
        data: {
            movieId: movieId,
            quantity: quantity
        },
        success: function(response) {
            console.log('Quantity updated successfully');
        },
        error: function(err) {
            console.error('Error updating quantity:', err);
        }
    });

    getCart();
});

$(document).on('click', '.remove-from-cart-btn', function() {
    const movieId = $(this).data('movie-id');

    $.ajax({
        dataType: "json",
        method: "POST",
        url: "api/shopping-cart?action=removeCart",
        data: {
            movieId: movieId
        },
        success: function(response) {
            console.log(`success removing movie ${movieId}`)
        },
        error: function(xhr, status, error) {
            console.error('Error removing item from cart:', error);
        }
    });

    getCart();
});




function proceedToPayment() {
    window.location.href = "payment.html";
}