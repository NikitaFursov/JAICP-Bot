theme: /

init:

    $global.createOrder = function() {
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

    $global.getSlot = function(parseTree, name) {
        var node = parseTree["_" + name];

        if (node !== undefined && node !== null) {
            return node;
        }

        return null;
    };

    $global.saveSlots = function(order, parseTree) {
        var v;

        v = $global.getSlot(parseTree, "PizzaName");
        if (v) order.pizzaName = v;

        v = $global.getSlot(parseTree, "PizzaSize");
        if (v) order.pizzaSize = v;

        v = $global.getSlot(parseTree, "PizzaBase");
        if (v) order.pizzaBase = v;

        v = $global.getSlot(parseTree, "PizzaTopping");
        if (v) order.pizzaTopping = v;

        v = $global.getSlot(parseTree, "Spiciness");
        if (v) order.spiciness = v;

        v = $global.getSlot(parseTree, "Quantity");
        if (v) order.quantity = Number(v);

        v = $global.getSlot(parseTree, "DeliveryTime");
        if (v) order.deliveryTime = v;

        v = $global.getSlot(parseTree, "PaymentMethod");
        if (v) order.paymentMethod = v;
    };

    $global.nextState = function(o) {
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

    $global.calc = function(o) {
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

    $global.orderText = function(o) {
        $global.calc(o);

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


state: Start
    q!: $regex</start>

    script:
        $session.order = $global.createOrder();

    a: Здравствуйте! Я бот пиццерии.
    a: Помогу оформить заказ.
    a: Например: «Хочу большую острую Американу на тонком тесте с пепперони».


state: Greeting
    intent!: /Greeting

    a: Здравствуйте! Чем могу помочь?


state: OrderPizza
    intent!: /OrderPizza

    script:
        if (!$session.order) {
            $session.order = $global.createOrder();
        }

        $global.saveSlots(
            $session.order,
            $parseTree
        );

        $global.calc(
            $session.order
        );

        $reactions.transition(
            $global.nextState(
                $session.order
            )
        );


state: ContinueOrder
    script:
        $reactions.transition(
            $global.nextState(
                $session.order
            )
        );


state: AskPizzaName

    a: Какую пиццу хотите?
    a: Например: Маргариту, Американу или Сохо.

    state: Receive
        intent: /SetPizza

        script:
            $session.order.pizzaName =
                $global.getSlot(
                    $parseTree,
                    "PizzaName"
                );

        a: Пицца сохранена.
        go!: /ContinueOrder


state: AskPizzaSize

    a: Какой размер пиццы выбрать?
    a: Маленький, средний или большой?

    state: Receive
        intent: /SetSize

        script:
            $session.order.pizzaSize =
                $global.getSlot(
                    $parseTree,
                    "PizzaSize"
                );

        a: Размер сохранён.
        go!: /ContinueOrder


state: AskPizzaBase

    a: Какую основу выбрать?
    a: Тонкую, толстую или американскую?

    state: Receive
        intent: /SetBase

        script:
            $session.order.pizzaBase =
                $global.getSlot(
                    $parseTree,
                    "PizzaBase"
                );

        a: Основа сохранена.
        go!: /ContinueOrder


state: AskPizzaTopping

    a: Какую начинку добавить?
    a: Например: пепперони, моцареллу или оливки.

    state: Receive
        intent: /SetTopping

        script:
            $session.order.pizzaTopping =
                $global.getSlot(
                    $parseTree,
                    "PizzaTopping"
                );

        a: Начинка сохранена.
        go!: /ContinueOrder


state: AskSpiciness

    a: Какую остроту выбрать?
    a: Неострую, среднюю или острую?

    state: Receive
        intent: /SetSpiciness

        script:
            $session.order.spiciness =
                $global.getSlot(
                    $parseTree,
                    "Spiciness"
                );

        a: Острота сохранена.
        go!: /ContinueOrder


state: AskQuantity

    a: Сколько пицц заказать?

    state: Receive
        intent: /SetQuantity

        script:
            $session.order.quantity =
                Number(
                    $global.getSlot(
                        $parseTree,
                        "Quantity"
                    )
                );

        a: Количество сохранено.
        go!: /ContinueOrder


state: AskAddress

    a: Укажите полный адрес доставки.

    state: Receive
        q: *

        script:
            $session.order.address =
                $request.query;

        a: Адрес доставки сохранён.
        go!: /ContinueOrder


state: AskPhone

    a: Укажите номер телефона для связи с курьером.

    state: Receive
        q: *

        script:
            $session.order.phone =
                $request.query;

        a: Номер телефона сохранён.
        go!: /ContinueOrder


state: AskDeliveryTime

    a: Когда доставить заказ?
    a: Например: «к 18:00», «через час» или «сегодня вечером».

    state: Receive
        intent: /SetDeliveryTime

        script:
            $session.order.deliveryTime =
                $global.getSlot(
                    $parseTree,
                    "DeliveryTime"
                );

        a: Время доставки сохранено.
        go!: /ContinueOrder


state: AskPaymentMethod

    a: Как будете оплачивать заказ?
    a: Наличными или картой?

    state: Receive
        intent: /SetPaymentMethod

        script:
            $session.order.paymentMethod =
                $global.getSlot(
                    $parseTree,
                    "PaymentMethod"
                );

        a: Способ оплаты сохранён.
        go!: /ContinueOrder


state: CheckOrder

    a: {{$global.orderText($session.order)}}
    a: Всё верно? Ответьте «да» или «нет».

    state: Yes
        intent: /ConfirmOrder

        a: Заказ подтверждён!
        go!: /CompleteOrder

    state: No
        intent: /RejectOrder

        a: Хорошо. Что хотите изменить?


state: CompleteOrder

    script:
        $global.calc($session.order);

    a: Заказ оформлен!
    a: {{$global.orderText($session.order)}}
    a: Спасибо за заказ!

    script:
        $jsapi.stopSession();


state: CancelOrder
    intent!: /CancelOrder

    a: Заказ отменён.

    script:
        $jsapi.stopSession();


state: Help
    intent!: /Help

    a: Можно сразу написать несколько параметров заказа, например:
    a: «Хочу большую острую Американу на тонком тесте с пепперони».


state: NoMatch
    event!: noMatch

    a: Не совсем понял вас. Попробуйте сформулировать запрос ещё раз.