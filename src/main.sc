require: functions.js

theme: /

state: Start
    q!: $regex</start>

    script:
        $session.order = global.createOrder();

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
            $session.order = global.createOrder();
        }

        global.saveSlots(
            $session.order,
            $parseTree
        );

        global.calc(
            $session.order
        );

        $reactions.transition(
            global.nextState(
                $session.order
            )
        );

theme: /

state: Start
    q!: $regex</start>

    script:
        $session.order = global.createOrder();

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
            $session.order = global.createOrder();
        }

        global.saveSlots(
            $session.order,
            $parseTree
        );

        global.calc(
            $session.order
        );

        $reactions.transition(
            global.nextState(
                $session.order
            )
        );

theme: /

state: Start
    q!: $regex</start>

    script:
        $session.order = global.createOrder();

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
            $session.order = global.createOrder();
        }

        global.saveSlots(
            $session.order,
            $parseTree
        );

        global.calc(
            $session.order
        );

        $reactions.transition(
            global.nextState(
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