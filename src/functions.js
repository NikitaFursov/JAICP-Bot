
function createOrder() {
    return {
        pizza: null,
        size: null,
        base: null,
        toppings: [],
        spiciness: null,
        quantity: null,

        deliveryAddress: null,
        phone: null,
        deliveryTime: null,
        paymentMethod: null,
        deliveryComment: null,

        foodPrice: 0,
        deliveryPrice: 0,
        totalPrice: 0
    };
}


function toArray(value) {
    if (!value) {
        return [];
    }

    if (Array.isArray(value)) {
        return value;
    }

    return [value];
}


function applyOrderSlots(order, parseTree) {

    if (parseTree._pizza) {
        order.pizza = parseTree._pizza;
    }

    if (parseTree._size) {
        order.size = parseTree._size;
    }

    if (parseTree._base) {
        order.base = parseTree._base;
    }

    if (parseTree._toppings) {
        order.toppings = toArray(parseTree._toppings);
    }

    if (parseTree._spiciness) {
        order.spiciness = parseTree._spiciness;
    }

    if (parseTree._quantity) {
        order.quantity = parseTree._quantity;
    }
}


function getToppingsText(toppings) {

    if (!toppings || toppings.length === 0) {
        return "не выбрана";
    }

    var names = [];

    for (var i = 0; i < toppings.length; i++) {

        if (toppings[i] && toppings[i].name) {
            names.push(toppings[i].name);
        } else {
            names.push(String(toppings[i]));
        }
    }

    return names.join(", ");
}


function getDeliveryTimeText(time) {

    if (!time) {
        return "не указано";
    }

    if (typeof time === "string") {
        return time;
    }

    if (
        typeof time.hour !== "undefined" &&
        typeof time.minute !== "undefined"
    ) {
        var minutes = String(time.minute);

        if (minutes.length === 1) {
            minutes = "0" + minutes;
        }

        return String(time.hour) + ":" + minutes;
    }

    if (time.value) {
        return String(time.value);
    }

    return String(time);
}


function updatePrices(order) {

    var onePizzaPrice = 0;

    if (order.pizza && order.pizza.basePrice) {
        onePizzaPrice += Number(
            order.pizza.basePrice
        );
    }

    if (order.size && order.size.priceAdd) {
        onePizzaPrice += Number(
            order.size.priceAdd
        );
    }

    if (order.base && order.base.priceAdd) {
        onePizzaPrice += Number(
            order.base.priceAdd
        );
    }

    if (order.toppings) {

        for (
            var i = 0;
            i < order.toppings.length;
            i++
        ) {

            if (
                order.toppings[i] &&
                order.toppings[i].priceAdd
            ) {

                onePizzaPrice += Number(
                    order.toppings[i].priceAdd
                );
            }
        }
    }

    var quantity = Number(order.quantity);

    if (!quantity || quantity < 1) {
        quantity = 1;
    }

    order.foodPrice =
        onePizzaPrice * quantity;

    if (order.foodPrice >= 2000) {
        order.deliveryPrice = 0;
    } else {
        order.deliveryPrice = 150;
    }

    order.totalPrice =
        order.foodPrice +
        order.deliveryPrice;
}


function orderIsComplete(order) {

    if (!order) {
        return false;
    }

    if (!order.pizza) {
        return false;
    }

    if (!order.size) {
        return false;
    }

    if (!order.base) {
        return false;
    }

    if (
        !order.toppings ||
        order.toppings.length === 0
    ) {
        return false;
    }

    if (!order.spiciness) {
        return false;
    }

    if (!order.quantity) {
        return false;
    }

    if (!order.deliveryAddress) {
        return false;
    }

    if (!order.phone) {
        return false;
    }

    if (!order.deliveryTime) {
        return false;
    }

    if (!order.paymentMethod) {
        return false;
    }

    return true;
}


function getOrderText(order) {

    if (!order) {
        return "Заказ пока пуст.";
    }

    var pizza = "не выбрана";
    var size = "не выбран";
    var base = "не выбрана";
    var spiciness = "не выбрана";
    var quantity = "не указано";

    var address = "не указан";
    var phone = "не указан";
    var deliveryTime = "не указано";
    var payment = "не выбран";
    var comment = "нет";

    if (order.pizza) {
        pizza =
            order.pizza.name ||
            String(order.pizza);
    }

    if (order.size) {
        size =
            order.size.name ||
            String(order.size);
    }

    if (order.base) {
        base =
            order.base.name ||
            String(order.base);
    }

    if (order.spiciness) {
        spiciness =
            order.spiciness.name ||
            String(order.spiciness);
    }

    if (order.quantity) {
        quantity =
            String(order.quantity);
    }

    if (order.deliveryAddress) {
        address =
            order.deliveryAddress;
    }

    if (order.phone) {
        phone =
            order.phone;
    }

    if (order.deliveryTime) {
        deliveryTime =
            getDeliveryTimeText(
                order.deliveryTime
            );
    }

    if (order.paymentMethod) {
        payment =
            order.paymentMethod.name ||
            String(order.paymentMethod);
    }

    if (order.deliveryComment) {
        comment =
            order.deliveryComment;
    }

    var toppings =
        getToppingsText(
            order.toppings
        );

    return "Ваш заказ:\n" +
        "Пицца: " + pizza + "\n" +
        "Размер: " + size + "\n" +
        "Основа: " + base + "\n" +
        "Начинка: " + toppings + "\n" +
        "Острота: " + spiciness + "\n" +
        "Количество: " + quantity + "\n\n" +

        "Доставка:\n" +
        "Адрес: " + address + "\n" +
        "Телефон: " + phone + "\n" +
        "Время: " + deliveryTime + "\n" +
        "Оплата: " + payment + "\n" +
        "Комментарий: " + comment;
}