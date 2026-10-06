global.createOrder = function() {
    return {
        pizzaName: null,
        pizzaSize: null,
        pizzaBase: null,
        pizzaTopping: null,
        spiciness: null,
        quantity: null,
        address: null,
        phone: null,
        deliveryTime: null,
        paymentMethod: null,
        unitPrice: 0,
        subtotal: 0,
        delivery: 0,
        total: 0
    };
};

global.getSlot = function(parseTree, name) {
    var node = parseTree["_" + name];

    if (node !== undefined && node !== null) {
        return node;
    }

    return null;
};

global.saveSlots = function(order, parseTree) {
    var v;

    v = global.getSlot(parseTree, "PizzaName");
    if (v) order.pizzaName = v;

    v = global.getSlot(parseTree, "PizzaSize");
    if (v) order.pizzaSize = v;

    v = global.getSlot(parseTree, "PizzaBase");
    if (v) order.pizzaBase = v;

    v = global.getSlot(parseTree, "PizzaTopping");
    if (v) order.pizzaTopping = v;

    v = global.getSlot(parseTree, "Spiciness");
    if (v) order.spiciness = v;

    v = global.getSlot(parseTree, "Quantity");
    if (v) order.quantity = Number(v);

    v = global.getSlot(parseTree, "DeliveryTime");
    if (v) order.deliveryTime = v;

    v = global.getSlot(parseTree, "PaymentMethod");
    if (v) order.paymentMethod = v;
};

global.nextState = function(o) {
    var m = [];

    if (!o.pizzaName) m.push("/AskPizzaName");
    if (!o.pizzaSize) m.push("/AskPizzaSize");
    if (!o.pizzaBase) m.push("/AskPizzaBase");
    if (!o.pizzaTopping) m.push("/AskPizzaTopping");
    if (!o.spiciness) m.push("/AskSpiciness");
    if (!o.quantity) m.push("/AskQuantity");
    if (!o.address) m.push("/AskAddress");
    if (!o.phone) m.push("/AskPhone");
    if (!o.deliveryTime) m.push("/AskDeliveryTime");
    if (!o.paymentMethod) m.push("/AskPaymentMethod");

    if (m.length === 0) {
        return "/CheckOrder";
    }

    return m[$jsapi.random(m.length)];
};

global.calc = function(o) {
    var prices = {
        "Маргарита": 550,
        "Американа": 650,
        "Американа Hot": 750,
        "Сохо": 700
    };

    var p = prices[o.pizzaName] || 600;

    if (o.pizzaSize === "большая") p += 100;
    if (o.pizzaSize === "маленькая") p -= 50;

    if (o.pizzaBase === "толстая") p += 100;
    if (o.pizzaBase === "американская") p += 150;

    if (o.pizzaTopping === "пепперони") p += 150;
    if (o.pizzaTopping === "моцарелла") p += 80;
    if (o.pizzaTopping === "томаты") p += 50;
    if (o.pizzaTopping === "оливки") p += 60;
    if (o.pizzaTopping === "халапеньо") p += 70;

    if (o.spiciness === "средняя") p += 50;
    if (o.spiciness === "острая") p += 80;

    o.unitPrice = p;
    o.subtotal = p * (Number(o.quantity) || 1);
    o.delivery = o.subtotal >= 2000 ? 0 : 150;
    o.total = o.subtotal + o.delivery;
};

global.orderText = function(o) {
    global.calc(o);

    return "Заказ:\n"
        + "Пицца: " + (o.pizzaName || "—") + "\n"
        + "Размер: " + (o.pizzaSize || "—") + "\n"
        + "Основа: " + (o.pizzaBase || "—") + "\n"
        + "Начинка: " + (o.pizzaTopping || "—") + "\n"
        + "Острота: " + (o.spiciness || "—") + "\n"
        + "Количество: " + (o.quantity || "—") + "\n"
        + "Адрес: " + (o.address || "—") + "\n"
        + "Телефон: " + (o.phone || "—") + "\n"
        + "Время: " + (o.deliveryTime || "—") + "\n"
        + "Оплата: " + (o.paymentMethod || "—") + "\n"
        + "Итого: " + o.total + " ₽";
};