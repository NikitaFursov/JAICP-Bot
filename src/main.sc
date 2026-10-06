require: functions.js

theme: /


    state: Start
        q!: $regex</start>

        script:
            $session.order = createOrder();

        a: Здравствуйте! Я бот пиццерии.
        a: Помогу оформить заказ пиццы.
        a: Вы можете сразу написать, например: «Хочу большую острую Американу на тонком тесте с пепперони».


    state: Hello
        q!: * (привет|здравствуй|здравствуйте|добрый день) *

        a: Здравствуйте! Какую пиццу хотите заказать?



    state: OrderPizza
        intent!: /OrderPizza

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }

            applyOrderSlots(
                $session.order,
                $parseTree
            );

        a: Хорошо, запоминаю ваш заказ.

        go!: /CheckOrder



    state: SetPizza
        intent!: /SetPizza

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }

            if ($parseTree._pizza) {
                $session.order.pizza =
                    $parseTree._pizza;
            }

        if: $session.order.pizza
            a: Выбрана пицца: {{$session.order.pizza.name}}.

        go!: /CheckOrder



    state: SetSize
        intent!: /SetSize

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }

            if ($parseTree._size) {
                $session.order.size =
                    $parseTree._size;
            }

        if: $session.order.size
            a: Размер изменён: {{$session.order.size.name}}.

        go!: /CheckOrder



    state: SetBase
        intent!: /SetBase

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }

            if ($parseTree._base) {
                $session.order.base =
                    $parseTree._base;
            }

        if: $session.order.base
            a: Основа: {{$session.order.base.name}}.

        go!: /CheckOrder



    state: SetTopping
        intent!: /SetTopping

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }

            if ($parseTree._toppings) {
                $session.order.toppings =
                    toArray(
                        $parseTree._toppings
                    );
            }

        a: Начинка сохранена.

        go!: /CheckOrder



    state: SetSpiciness
        intent!: /SetSpiciness

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }

            if ($parseTree._spiciness) {
                $session.order.spiciness =
                    $parseTree._spiciness;
            }

        if: $session.order.spiciness
            a: Острота: {{$session.order.spiciness.name}}.

        go!: /CheckOrder



    state: SetQuantity
        intent!: /SetQuantity

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }

            if ($parseTree._quantity) {
                $session.order.quantity =
                    $parseTree._quantity;
            }

        if: $session.order.quantity
            a: Количество: {{$session.order.quantity}}.

        go!: /CheckOrder



    state: SetDeliveryTime
        intent!: /SetDeliveryTime

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }

            if ($parseTree._deliveryTime) {
                $session.order.deliveryTime =
                    $parseTree._deliveryTime;
            }

        if: $session.order.deliveryTime
            a: Время доставки сохранено.

        go!: /CheckOrder



    state: SetPaymentMethod
        intent!: /SetPaymentMethod

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }

            if ($parseTree._payment) {
                $session.order.paymentMethod =
                    $parseTree._payment;
            }

        if: $session.order.paymentMethod
            a: Способ оплаты: {{$session.order.paymentMethod.name}}.

        go!: /CheckOrder



    state: CheckOrder

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }


            var missing = [];


            if (!$session.order.pizza) {
                missing.push("pizza");
            }


            if (!$session.order.size) {
                missing.push("size");
            }


            if (!$session.order.base) {
                missing.push("base");
            }


            if (
                !$session.order.toppings ||
                $session.order.toppings.length === 0
            ) {
                missing.push("toppings");
            }


            if (!$session.order.spiciness) {
                missing.push("spiciness");
            }


            if (!$session.order.quantity) {
                missing.push("quantity");
            }


            if (!$session.order.deliveryAddress) {
                missing.push("address");
            }


            if (!$session.order.phone) {
                missing.push("phone");
            }


            if (!$session.order.deliveryTime) {
                missing.push("deliveryTime");
            }


            if (!$session.order.paymentMethod) {
                missing.push("payment");
            }


            if (missing.length === 0) {

                $reactions.transition(
                    "/ConfirmOrder"
                );

            } else {

                var index =
                    $jsapi.random(
                        missing.length
                    );

                var parameter =
                    missing[index];


                $session.missingParameter =
                    parameter;


                if (parameter === "pizza") {

                    $reactions.transition(
                        "/AskPizza"
                    );

                } else if (
                    parameter === "size"
                ) {

                    $reactions.transition(
                        "/AskSize"
                    );

                } else if (
                    parameter === "base"
                ) {

                    $reactions.transition(
                        "/AskBase"
                    );

                } else if (
                    parameter === "toppings"
                ) {

                    $reactions.transition(
                        "/AskTopping"
                    );

                } else if (
                    parameter === "spiciness"
                ) {

                    $reactions.transition(
                        "/AskSpiciness"
                    );

                } else if (
                    parameter === "quantity"
                ) {

                    $reactions.transition(
                        "/AskQuantity"
                    );

                } else if (
                    parameter === "address"
                ) {

                    $reactions.transition(
                        "/AskAddress"
                    );

                } else if (
                    parameter === "phone"
                ) {

                    $reactions.transition(
                        "/AskPhone"
                    );

                } else if (
                    parameter ===
                    "deliveryTime"
                ) {

                    $reactions.transition(
                        "/AskDeliveryTime"
                    );

                } else if (
                    parameter === "payment"
                ) {

                    $reactions.transition(
                        "/AskPaymentMethod"
                    );
                }
            }



    state: AskPizza

        a: Какую пиццу хотите?
        a: Например: Маргариту, Американу, Американа Hot или Сохо.



    state: AskSize

        a: Какой размер пиццы выбрать?
        a: Маленький, средний или большой?



    state: AskBase

        a: Какую основу выбрать?
        a: Тонкую или пышную?



    state: AskTopping

        a: Какую начинку добавить?
        a: Например: моцареллу, пепперони, томаты, оливки или халапеньо.



    state: AskSpiciness

        a: Какую остроту выбрать?
        a: Неострую, средней остроты или острую?



    state: AskQuantity

        a: Сколько пицц заказать?



    state: AskAddress

        InputText:
            prompt = Укажите полный адрес доставки. Например: ул. Ленина, д. 15, кв. 24
            varName = tempAddress
            then = /SaveAddress



    state: SaveAddress

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }

            $session.order.deliveryAddress =
                $session.tempAddress;

            delete $session.tempAddress;

        a: Адрес доставки сохранён: {{$session.order.deliveryAddress}}.

        go!: /CheckOrder



    state: AskPhone

        InputPhoneNumber:
            prompt = Укажите номер телефона для связи с курьером.
            varName = tempPhone
            then = /SavePhone



    state: SavePhone

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }

            $session.order.phone =
                $session.tempPhone;

            delete $session.tempPhone;

        a: Номер телефона сохранён.

        go!: /CheckOrder



    state: AskDeliveryTime

        a: Когда доставить заказ?
        a: Например: «к 18:00», «через час» или «сегодня вечером».



    state: AskPaymentMethod

        a: Как будете оплачивать заказ?

        buttons:
            "Наличными" -> /PaymentCash
            "Картой" -> /PaymentCard
            "Онлайн" -> /PaymentOnline



    state: PaymentCash

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }

            $session.order.paymentMethod = {
                id: "cash",
                name: "наличными"
            };

        a: Оплата наличными.

        go!: /CheckOrder



    state: PaymentCard

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }

            $session.order.paymentMethod = {
                id: "card_on_delivery",
                name: "картой при получении"
            };

        a: Оплата картой при получении.

        go!: /CheckOrder



    state: PaymentOnline

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }

            $session.order.paymentMethod = {
                id: "online",
                name: "онлайн"
            };

        a: Выбрана онлайн-оплата.

        go!: /CheckOrder



    state: ConfirmOrder

        script:

            updatePrices(
                $session.order
            );

            $reactions.answer(
                getOrderText(
                    $session.order
                )
            );


            $reactions.answer(
                "Стоимость пиццы: " +
                $session.order.foodPrice +
                " ₽."
            );


            if (
                $session.order.deliveryPrice === 0
            ) {

                $reactions.answer(
                    "Доставка: бесплатно."
                );

            } else {

                $reactions.answer(
                    "Доставка: " +
                    $session.order.deliveryPrice +
                    " ₽."
                );
            }


            $reactions.answer(
                "Итого: " +
                $session.order.totalPrice +
                " ₽."
            );


            $reactions.answer(
                "Всё верно?"
            );

        buttons:
            "Подтвердить" -> /CompleteOrder
            "Изменить" -> /ChangeOrder
            "Комментарий" -> /AskDeliveryComment
            "Отменить" -> /CancelByButton



    state: AskDeliveryComment

        InputText:
            prompt = Напишите комментарий для курьера. Например: «подъезд 2, домофон 24». Если комментария нет, напишите «нет».
            varName = tempDeliveryComment
            then = /SaveDeliveryComment



    state: SaveDeliveryComment

        script:

            if (!$session.order) {
                $session.order =
                    createOrder();
            }

            if (
                $session.tempDeliveryComment ===
                "нет"
            ) {

                $session.order.deliveryComment =
                    null;

            } else {

                $session.order.deliveryComment =
                    $session.tempDeliveryComment;
            }


            delete $session.tempDeliveryComment;

        a: Комментарий к доставке сохранён.

        go!: /ConfirmOrder



    state: ConfirmByIntent
        intent!: /ConfirmOrder

        if: $session.order && orderIsComplete($session.order)
            go!: /CompleteOrder

        else:
            a: В заказе ещё не хватает некоторых данных.
            go!: /CheckOrder



    state: ChangeOrder
        intent!: /ChangeOrder

        a: Что хотите изменить?

        buttons:
            "Пиццу" -> /AskPizza
            "Размер" -> /AskSize
            "Основу" -> /AskBase
            "Начинку" -> /AskTopping
            "Остроту" -> /AskSpiciness
            "Количество" -> /AskQuantity
            "Адрес" -> /AskAddress
            "Телефон" -> /AskPhone
            "Время" -> /AskDeliveryTime
            "Оплату" -> /AskPaymentMethod
            "Комментарий" -> /AskDeliveryComment
            "Назад" -> /ConfirmOrder



    state: ShowOrder
        intent!: /ShowOrder

        script:

            if (!$session.order) {

                $reactions.answer(
                    "Сейчас активного заказа нет."
                );

            } else {

                updatePrices(
                    $session.order
                );

                $reactions.answer(
                    getOrderText(
                        $session.order
                    )
                );


                if (
                    $session.order.pizza &&
                    $session.order.quantity
                ) {

                    $reactions.answer(
                        "Текущая стоимость: " +
                        $session.order.totalPrice +
                        " ₽."
                    );
                }
            }



    state: CompleteOrder

        script:

            if (!$session.order) {

                $reactions.answer(
                    "Активного заказа нет."
                );

            } else {

                updatePrices(
                    $session.order
                );


                $reactions.answer(
                    "✅ Заказ подтверждён!"
                );


                $reactions.answer(
                    getOrderText(
                        $session.order
                    )
                );


                if (
                    $session.order.deliveryPrice === 0
                ) {

                    $reactions.answer(
                        "Доставка бесплатная."
                    );

                } else {

                    $reactions.answer(
                        "Стоимость доставки: " +
                        $session.order.deliveryPrice +
                        " ₽."
                    );
                }


                $reactions.answer(
                    "Итоговая стоимость: " +
                    $session.order.totalPrice +
                    " ₽."
                );


                $reactions.answer(
                    "🍕 Передаём заказ на кухню."
                );


                $session.lastOrder =
                    $session.order;


                $session.order = null;


                delete $session.missingParameter;
            }



    state: CancelOrder
        intent!: /CancelOrder

        script:

            $session.order = null;

            delete $session.missingParameter;

        a: Заказ отменён.
        a: Чтобы оформить новый заказ, просто напишите, какую пиццу хотите.



    state: CancelByButton

        script:

            $session.order = null;

            delete $session.missingParameter;

        a: Заказ отменён.
        a: Чтобы оформить новый заказ, просто напишите, какую пиццу хотите.



    state: NoMatch || noContext=true
        event!: noMatch

        a: Не совсем понял вас.
        a: Например, можно написать: «Хочу большую Американу на тонком тесте».