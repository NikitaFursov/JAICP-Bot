init:

    /*
     * ============================================================
     * ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ
     * ============================================================
     */

    $global.hasValue = function(value) {

        if (value === null || value === undefined || value === "") {
            return false;
        }

        if (Array.isArray(value) && value.length === 0) {
            return false;
        }

        return true;
    };


    /*
     * Приведение значения к строке.
     * Если DATA сущности содержит JSON,
     * пытаемся взять понятное пользователю имя.
     */
    $global.valueToText = function(value) {

        if (value === null || value === undefined) {
            return "";
        }

        if (Array.isArray(value)) {
            var result = [];

            for (var i = 0; i < value.length; i++) {
                result.push(
                    $global.valueToText(value[i])
                );
            }

            return result.join(", ");
        }

        if (typeof value === "object") {

            if (value.name !== undefined) {
                return String(value.name);
            }

            if (value.title !== undefined) {
                return String(value.title);
            }

            if (value.label !== undefined) {
                return String(value.label);
            }

            if (value.value !== undefined) {
                return String(value.value);
            }

            if (value.text !== undefined) {
                return String(value.text);
            }

            return JSON.stringify(value);
        }

        return String(value);
    };


    /*
     * Получаем slotData из $parseTree.
     *
     * Основной вариант:
     * $parseTree._PizzaName.slotData
     *
     * Именно эти данные затем сохраняются в $session.order.
     */
    $global.getSlotValue = function(parseTree, slotName) {

        if (!parseTree) {
            return null;
        }

        var node = parseTree["_" + slotName];

        if (node) {

            if (node.slotData !== undefined) {
                return node.slotData;
            }

            if (node.value !== undefined) {
                return node.value;
            }

            if (node.text !== undefined) {
                return node.text;
            }
        }


        /*
         * Дополнительная поддержка system entity
         * @duckling.number для количества.
         */
        if (slotName === "Quantity") {

            var numberNode = parseTree["_duckling.number"];

            if (numberNode) {

                if (numberNode.slotData !== undefined) {
                    return numberNode.slotData;
                }

                if (numberNode.value !== undefined) {
                    return numberNode.value;
                }

                if (numberNode.text !== undefined) {
                    return numberNode.text;
                }
            }

            var simpleNumberNode = parseTree["_number"];

            if (simpleNumberNode) {

                if (simpleNumberNode.slotData !== undefined) {
                    return simpleNumberNode.slotData;
                }

                if (simpleNumberNode.value !== undefined) {
                    return simpleNumberNode.value;
                }

                if (simpleNumberNode.text !== undefined) {
                    return simpleNumberNode.text;
                }
            }
        }


        return null;
    };


    /*
     * Создание нового заказа.
     */
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

            pizzaUnitPrice: 0,
            subtotal: 0,
            deliveryPrice: 0,
            total: 0
        };
    };


    /*
     * Сохраняем все параметры,
     * которые были распознаны в ТЕКУЩЕЙ фразе.
     *
     * Важный момент:
     * существующие значения не удаляются,
     * а новые только обновляют уже известные.
     */
    $global.applyOrderSlots = function(order, parseTree) {

        var slots = [
            "PizzaName",
            "PizzaSize",
            "PizzaBase",
            "PizzaTopping",
            "Spiciness",
            "Quantity",
            "Address",
            "Phone",
            "DeliveryTime",
            "PaymentMethod"
        ];


        for (var i = 0; i < slots.length; i++) {

            var slotName = slots[i];

            var value = $global.getSlotValue(
                parseTree,
                slotName
            );


            if (!$global.hasValue(value)) {
                continue;
            }


            var sessionName =
                slotName.charAt(0).toLowerCase()
                + slotName.substring(1);


            /*
             * Количество храним именно числом.
             */
            if (slotName === "Quantity") {

                var quantity = Number(
                    $global.valueToText(value)
                );

                if (!isNaN(quantity) && quantity > 0) {
                    order.quantity = quantity;
                }

                continue;
            }


            order[sessionName] =
                $global.valueToText(value);
        }
    };


    /*
     * Нормализация строки для поиска цены.
     */
    $global.key = function(value) {

        return String(
            value || ""
        )
        .toLowerCase()
        .replace(/ё/g, "е")
        .replace(/\s+/g, " ")
        .trim();
    };


    /*
     * ============================================================
     * ЦЕНА
     * ============================================================
     */

    $global.updatePrices = function(order) {

        var pizza = $global.key(order.pizzaName);
        var size = $global.key(order.pizzaSize);
        var base = $global.key(order.pizzaBase);
        var topping = $global.key(order.pizzaTopping);
        var spicy = $global.key(order.spiciness);


        /*
         * Базовые цены пицц.
         * При необходимости поменяйте значения здесь.
         */
        var pizzaPrices = {
            "маргарита": 550,
            "американа": 650,
            "американа hot": 750,
            "американа хот": 750,
            "американахот": 750,
            "сохо": 700
        };


        var unitPrice = 0;


        if (pizzaPrices[pizza] !== undefined) {
            unitPrice = pizzaPrices[pizza];
        }


        /*
         * Размер.
         */
        if (
            size === "большая" ||
            size === "большой" ||
            size === "35 см"
        ) {
            unitPrice += 100;
        }

        else if (
            size === "маленькая" ||
            size === "маленький" ||
            size === "мини" ||
            size === "25 см"
        ) {
            unitPrice -= 50;
        }


        /*
         * Основа.
         */
        if (
            base === "толстая" ||
            base === "толстое" ||
            base === "толстое тесто" ||
            base === "пышная"
        ) {
            unitPrice += 100;
        }

        else if (
            base === "американская" ||
            base === "американская основа"
        ) {
            unitPrice += 150;
        }


        /*
         * Начинка.
         */
        if (
            topping === "пепперони"
        ) {
            unitPrice += 150;
        }

        else if (
            topping === "моцарелла" ||
            topping === "моцареллу"
        ) {
            unitPrice += 80;
        }

        else if (
            topping === "томаты" ||
            topping === "томаты"
        ) {
            unitPrice += 50;
        }

        else if (
            topping === "оливки"
        ) {
            unitPrice += 60;
        }

        else if (
            topping === "халапеньо" ||
            topping === "халапено"
        ) {
            unitPrice += 70;
        }


        /*
         * Острота.
         */
        if (
            spicy === "средняя" ||
            spicy === "средней остроты" ||
            spicy === "умеренно острая"
        ) {
            unitPrice += 50;
        }

        else if (
            spicy === "острая" ||
            spicy === "очень острая" ||
            spicy === "hot"
        ) {
            unitPrice += 80;
        }


        /*
         * Чтобы цена не стала отрицательной.
         */
        if (unitPrice < 0) {
            unitPrice = 0;
        }


        order.pizzaUnitPrice = unitPrice;


        var quantity = Number(order.quantity || 0);


        if (quantity > 0) {
            order.subtotal =
                unitPrice * quantity;
        }

        else {
            order.subtotal = 0;
        }


        /*
         * Доставка:
         * 150 ₽,
         * бесплатно от 2000 ₽.
         */
        if (order.subtotal >= 2000) {
            order.deliveryPrice = 0;
        }

        else if (order.subtotal > 0) {
            order.deliveryPrice = 150;
        }

        else {
            order.deliveryPrice = 0;
        }


        order.total =
            order.subtotal
            + order.deliveryPrice;
    };


    /*
     * ============================================================
     * ФОРМАТИРОВАНИЕ ЗАКАЗА
     * ============================================================
     */

    $global.getPizzaText = function(order) {

        var parts = [];


        if ($global.hasValue(order.pizzaSize)) {
            parts.push(
                $global.valueToText(order.pizzaSize)
            );
        }


        if ($global.hasValue(order.pizzaName)) {
            parts.push(
                $global.valueToText(order.pizzaName)
            );
        }


        if ($global.hasValue(order.spiciness)) {
            parts.push(
                $global.valueToText(order.spiciness)
            );
        }


        if ($global.hasValue(order.pizzaBase)) {
            parts.push(
                $global.valueToText(order.pizzaBase)
                + " тесто"
            );
        }


        if ($global.hasValue(order.pizzaTopping)) {
            parts.push(
                "с "
                + $global.valueToText(order.pizzaTopping)
            );
        }


        if (parts.length === 0) {
            return "пицца";
        }


        return parts.join(", ");
    };


    $global.getQuantityText = function(quantity) {

        var n = Number(quantity);

        if (n === 1) {
            return "1 пицца";
        }

        if (
            n >= 2 &&
            n <= 4
        ) {
            return n + " пиццы";
        }

        return n + " пицц";
    };


    $global.getOrderText = function(order) {

        $global.updatePrices(order);


        var text =
            "🍕 Ваш заказ:\n\n";


        if ($global.hasValue(order.pizzaName)) {
            text +=
                "Пицца: "
                + order.pizzaName
                + "\n";
        }


        if ($global.hasValue(order.pizzaSize)) {
            text +=
                "Размер: "
                + order.pizzaSize
                + "\n";
        }


        if ($global.hasValue(order.pizzaBase)) {
            text +=
                "Основа: "
                + order.pizzaBase
                + "\n";
        }


        if ($global.hasValue(order.pizzaTopping)) {
            text +=
                "Начинка: "
                + order.pizzaTopping
                + "\n";
        }


        if ($global.hasValue(order.spiciness)) {
            text +=
                "Острота: "
                + order.spiciness
                + "\n";
        }


        if ($global.hasValue(order.quantity)) {
            text +=
                "Количество: "
                + $global.getQuantityText(
                    order.quantity
                )
                + "\n";
        }


        if ($global.hasValue(order.address)) {
            text +=
                "Адрес: "
                + order.address
                + "\n";
        }


        if ($global.hasValue(order.phone)) {
            text +=
                "Телефон: "
                + order.phone
                + "\n";
        }


        if ($global.hasValue(order.deliveryTime)) {
            text +=
                "Доставка: "
                + order.deliveryTime
                + "\n";
        }


        if ($global.hasValue(order.paymentMethod)) {
            text +=
                "Оплата: "
                + order.paymentMethod
                + "\n";
        }


        text +=
            "\nЦена пиццы: "
            + order.pizzaUnitPrice
            + " ₽";


        text +=
            "\nСумма пицц: "
            + order.subtotal
            + " ₽";


        text +=
            "\nДоставка: "
            + (
                order.deliveryPrice === 0
                    ? "бесплатно"
                    : order.deliveryPrice + " ₽"
            );


        text +=
            "\nИтого: "
            + order.total
            + " ₽";


        return text;
    };


    /*
     * ============================================================
     * ПРОВЕРКА ЗАПОЛНЕННОСТИ
     * ============================================================
     */

    $global.orderIsComplete = function(order) {

        return (
            $global.hasValue(order.pizzaName) &&
            $global.hasValue(order.pizzaSize) &&
            $global.hasValue(order.pizzaBase) &&
            $global.hasValue(order.pizzaTopping) &&
            $global.hasValue(order.spiciness) &&
            $global.hasValue(order.quantity) &&
            $global.hasValue(order.address) &&
            $global.hasValue(order.phone) &&
            $global.hasValue(order.deliveryTime) &&
            $global.hasValue(order.paymentMethod)
        );
    };


    /*
     * Есть ли хотя бы один параметр пиццы.
     */
    $global.hasPizzaData = function(order) {

        return (
            $global.hasValue(order.pizzaName) ||
            $global.hasValue(order.pizzaSize) ||
            $global.hasValue(order.pizzaBase) ||
            $global.hasValue(order.pizzaTopping) ||
            $global.hasValue(order.spiciness)
        );
    };


    /*
     * ============================================================
     * ВЫБОР СЛЕДУЮЩЕГО НЕДОСТАЮЩЕГО ПАРАМЕТРА
     * ============================================================
     *
     * Название пиццы спрашиваем первым, если его нет.
     *
     * Остальные недостающие параметры выбираются случайно.
     * Таким образом сохраняется требование лабораторной
     * о нелинейном диалоге.
     */
    $global.nextOrderState = function(order) {

        /*
         * Сначала обязательно определяем пиццу.
         */
        if (!$global.hasValue(order.pizzaName)) {
            return "/AskPizzaName";
        }


        var missing = [];


        if (!$global.hasValue(order.pizzaSize)) {
            missing.push("/AskPizzaSize");
        }


        if (!$global.hasValue(order.pizzaBase)) {
            missing.push("/AskPizzaBase");
        }


        if (!$global.hasValue(order.pizzaTopping)) {
            missing.push("/AskPizzaTopping");
        }


        if (!$global.hasValue(order.spiciness)) {
            missing.push("/AskSpiciness");
        }


        if (!$global.hasValue(order.quantity)) {
            missing.push("/AskQuantity");
        }


        if (!$global.hasValue(order.address)) {
            missing.push("/AskAddress");
        }


        if (!$global.hasValue(order.phone)) {
            missing.push("/AskPhone");
        }


        if (!$global.hasValue(order.deliveryTime)) {
            missing.push("/AskDeliveryTime");
        }


        if (!$global.hasValue(order.paymentMethod)) {
            missing.push("/AskPaymentMethod");
        }


        /*
         * Если ничего не осталось —
         * заказ полностью заполнен.
         */
        if (missing.length === 0) {
            return "/CheckOrder";
        }


        /*
         * Случайно выбираем один недостающий этап.
         */
        return missing[
            $jsapi.random(missing.length)
        ];
    };


theme: /


    /*
     * ============================================================
     * START
     * ============================================================
     */

    state: Start
        q!: $regex</start>

        script:
            $session.order =
                $global.createOrder();

        a: Здравствуйте! Я бот пиццерии.
        a: Помогу оформить заказ пиццы.
        a: Вы можете сразу написать, например: «Хочу большую острую Американу на тонком тесте с пепперони».


    /*
     * ============================================================
     * ПРИВЕТСТВИЕ
     * ============================================================
     */

    state: Hello
        intent!: /Greeting

        a: Здравствуйте! Я бот пиццерии.
        a: Могу оформить заказ и рассчитать его стоимость.
        a: Например: «Хочу большую острую Американу на тонком тесте с пепперони».


    /*
     * ============================================================
     * ПЕРВОНАЧАЛЬНЫЙ ЗАКАЗ
     * ============================================================
     *
     * Это главный исправленный участок.
     *
     * Бот НЕ начинает сбор заказа с нуля.
     * Он сначала забирает ВСЕ найденные слоты из фразы.
     */
    state: OrderPizza
        intent!: /OrderPizza

        script:

            if (!$session.order) {
                $session.order =
                    $global.createOrder();
            }


            /*
             * Ключевой момент:
             * сохраняем ВСЕ распознанные параметры
             * из текущей фразы в $session.order.
             */
            $global.applyOrderSlots(
                $session.order,
                $parseTree
            );


            $global.updatePrices(
                $session.order
            );


        if: $global.hasPizzaData($session.order)
            a: Принял: {{$global.getPizzaText($session.order)}}.
            go!: /ContinueOrder

        else:
            a: Конечно. Давайте оформим заказ.
            go!: /AskPizzaName


    /*
     * ============================================================
     * ИЗМЕНЕНИЕ ПАРАМЕТРОВ
     * ============================================================
     *
     * Например:
     * «Измени размер на большую»
     *
     * Важно:
     * этот интент должен быть обучен именно фразам про изменение,
     * а не обычным заказам.
     */
    state: ChangeOrder
        intent!: /ChangeOrder

        script:

            if (!$session.order) {
                $session.order =
                    $global.createOrder();
            }


            var oldName =
                $session.order.pizzaName;


            $global.applyOrderSlots(
                $session.order,
                $parseTree
            );


            $global.updatePrices(
                $session.order
            );


            if (
                oldName !== null &&
                oldName !== undefined &&
                oldName !== $session.order.pizzaName
            ) {

                $reactions.answer(
                    "Пицца изменена на "
                    + $session.order.pizzaName
                    + "."
                );

                $reactions.transition(
                    "/ContinueOrder"
                );
            }

            else if (
                $global.hasPizzaData(
                    $session.order
                ) &&
                (
                    $parseTree._PizzaSize ||
                    $parseTree._PizzaBase ||
                    $parseTree._PizzaTopping ||
                    $parseTree._Spiciness ||
                    $parseTree._Quantity
                )
            ) {

                $reactions.answer(
                    "Изменение сохранено."
                );

                $reactions.transition(
                    "/ContinueOrder"
                );
            }

            else {

                $reactions.answer(
                    "Что хотите изменить?\n"
                    + "Например: «изменить размер на большую», "
                    + "«сделать острее» или "
                    + "«добавить моцареллу»."
                );

                $reactions.transition(
                    "/ChangeOrder/Wait"
                );
            }


        state: Wait

            /*
             * Изменение размера.
             */
            state: Size
                intent: /SetSize

                script:

                    var sizeValue =
                        $global.getSlotValue(
                            $parseTree,
                            "PizzaSize"
                        );


                    if ($global.hasValue(sizeValue)) {

                        $session.order.pizzaSize =
                            $global.valueToText(
                                sizeValue
                            );
                    }


                    $global.updatePrices(
                        $session.order
                    );

                a: Размер изменён.
                go!: /ContinueOrder


                /*
                 * Изменение основы.
                 */
                state: Base
                    intent: /SetBase

                    script:

                        var baseValue =
                            $global.getSlotValue(
                                $parseTree,
                                "PizzaBase"
                            );


                        if ($global.hasValue(baseValue)) {

                            $session.order.pizzaBase =
                                $global.valueToText(
                                    baseValue
                                );
                        }


                        $global.updatePrices(
                            $session.order
                        );

                    a: Основа изменена.
                    go!: /ContinueOrder


                /*
                 * Изменение начинки.
                 */
                state: Topping
                    intent: /SetTopping

                    script:

                        var toppingValue =
                            $global.getSlotValue(
                                $parseTree,
                                "PizzaTopping"
                            );


                        if ($global.hasValue(toppingValue)) {

                            $session.order.pizzaTopping =
                                $global.valueToText(
                                    toppingValue
                                );
                        }


                        $global.updatePrices(
                            $session.order
                        );

                    a: Начинка изменена.
                    go!: /ContinueOrder


                /*
                 * Изменение остроты.
                 */
                state: Spiciness
                    intent: /SetSpiciness

                    script:

                        var spicyValue =
                            $global.getSlotValue(
                                $parseTree,
                                "Spiciness"
                            );


                        if ($global.hasValue(spicyValue)) {

                            $session.order.spiciness =
                                $global.valueToText(
                                    spicyValue
                                );
                        }


                        $global.updatePrices(
                            $session.order
                        );

                    a: Острота изменена.
                    go!: /ContinueOrder


                /*
                 * Изменение пиццы по названию.
                 */
                state: PizzaName

                    q: * @PizzaName *

                    script:

                        var pizzaValue =
                            $global.getSlotValue(
                                $parseTree,
                                "PizzaName"
                            );


                        if ($global.hasValue(pizzaValue)) {

                            $session.order.pizzaName =
                                $global.valueToText(
                                    pizzaValue
                                );
                        }


                        $global.updatePrices(
                            $session.order
                        );

                    a: Пицца изменена.
                    go!: /ContinueOrder


                /*
                 * Изменение количества.
                 */
                state: Quantity

                    InputNumber:
                        prompt = Сколько пицц заказать?
                        minValue = 1
                        maxValue = 20
                        varName = newQuantity
                        failureMessage = ["Укажите количество от 1 до 20.", "Введите число от 1 до 20."]
                        then = /ChangeOrder/Wait/SaveQuantity

                state: SaveQuantity
                    script:

                        $session.order.quantity =
                            Number(
                                $session.newQuantity
                            );

                        $global.updatePrices(
                            $session.order
                        );

                    a: Количество изменено.
                    go!: /ContinueOrder


    /*
     * ============================================================
     * ПРОДОЛЖЕНИЕ ЗАКАЗА
     * ============================================================
     *
     * Здесь НИКОГДА не спрашиваем всё заново.
     *
     * Состояние смотрит на $session.order и выбирает
     * только отсутствующий параметр.
     */
    state: ContinueOrder

        script:

            if (!$session.order) {
                $session.order =
                    $global.createOrder();
            }


            $global.updatePrices(
                $session.order
            );


            $reactions.transition(
                $global.nextOrderState(
                    $session.order
                )
            );


    /*
     * ============================================================
     * НАЗВАНИЕ ПИЦЦЫ
     * ============================================================
     */

    state: AskPizzaName

        a: Какую пиццу хотите?
        a: Например: Маргариту, Американу, Американа Hot или Сохо.

        go!: /AskPizzaName/Receive


        state: Receive

            q: * @PizzaName *

            script:

                var pizzaValue =
                    $global.getSlotValue(
                        $parseTree,
                        "PizzaName"
                    );


                if ($global.hasValue(pizzaValue)) {

                    $session.order.pizzaName =
                        $global.valueToText(
                            pizzaValue
                        );
                }


            a: Пицца сохранена.
            go!: /ContinueOrder


            state: NoMatch
                event: noMatch

                a: Не удалось определить пиццу. Например: Маргарита, Американа или Сохо.
                go!: /AskPizzaName/Receive


    /*
     * ============================================================
     * РАЗМЕР
     * ============================================================
     */

    state: AskPizzaSize

        a: Какой размер пиццы выбрать?
        a: Маленький, средний или большой.

        go!: /AskPizzaSize/Receive


        state: Receive

            intent: /SetSize

            script:

                var sizeValue =
                    $global.getSlotValue(
                        $parseTree,
                        "PizzaSize"
                    );


                if ($global.hasValue(sizeValue)) {

                    $session.order.pizzaSize =
                        $global.valueToText(
                            sizeValue
                        );
                }


                $global.updatePrices(
                    $session.order
                );


            a: Размер сохранён.
            go!: /ContinueOrder


            state: NoMatch
                event: noMatch

                a: Укажите размер: маленькая, средняя или большая.
                go!: /AskPizzaSize/Receive


    /*
     * ============================================================
     * ОСНОВА
     * ============================================================
     */

    state: AskPizzaBase

        a: Какую основу выбрать?
        a: Например: тонкую, толстую или американскую.

        go!: /AskPizzaBase/Receive


        state: Receive

            intent: /SetBase

            script:

                var baseValue =
                    $global.getSlotValue(
                        $parseTree,
                        "PizzaBase"
                    );


                if ($global.hasValue(baseValue)) {

                    $session.order.pizzaBase =
                        $global.valueToText(
                            baseValue
                        );
                }


                $global.updatePrices(
                    $session.order
                );


            a: Основа сохранена.
            go!: /ContinueOrder


            state: NoMatch
                event: noMatch

                a: Укажите основу: тонкая, толстая или американская.
                go!: /AskPizzaBase/Receive


    /*
     * ============================================================
     * НАЧИНКА
     * ============================================================
     */

    state: AskPizzaTopping

        a: Какую начинку добавить?
        a: Например: моцареллу, пепперони, томаты, оливки или халапеньо.

        go!: /AskPizzaTopping/Receive


        state: Receive

            intent: /SetTopping

            script:

                var toppingValue =
                    $global.getSlotValue(
                        $parseTree,
                        "PizzaTopping"
                    );


                if ($global.hasValue(toppingValue)) {

                    $session.order.pizzaTopping =
                        $global.valueToText(
                            toppingValue
                        );
                }


                $global.updatePrices(
                    $session.order
                );


            a: Начинка сохранена.
            go!: /ContinueOrder


            state: NoMatch
                event: noMatch

                a: Например: пепперони, моцареллу, томаты, оливки или халапеньо.
                go!: /AskPizzaTopping/Receive


    /*
     * ============================================================
     * ОСТРОТА
     * ============================================================
     */

    state: AskSpiciness

        a: Какую остроту выбрать?
        a: Неострую, средней остроты или острую.

        go!: /AskSpiciness/Receive


        state: Receive

            intent: /SetSpiciness

            script:

                var spicyValue =
                    $global.getSlotValue(
                        $parseTree,
                        "Spiciness"
                    );


                if ($global.hasValue(spicyValue)) {

                    $session.order.spiciness =
                        $global.valueToText(
                            spicyValue
                        );
                }


                $global.updatePrices(
                    $session.order
                );


            a: Острота сохранена.
            go!: /ContinueOrder


            state: NoMatch
                event: noMatch

                a: Выберите неострую, среднюю или острую пиццу.
                go!: /AskSpiciness/Receive


    /*
     * ============================================================
     * КОЛИЧЕСТВО
     * ============================================================
     *
     * Здесь InputNumber защищает от ситуации,
     * когда "14" случайно распознаётся как что-то другое.
     */
    state: AskQuantity

        InputNumber:
            prompt = Сколько пицц заказать?
            minValue = 1
            maxValue = 20
            varName = quantityInput
            failureMessage = ["Укажите количество от 1 до 20.", "Введите число от 1 до 20."]
            then = /SaveQuantity


    state: SaveQuantity

        script:

            $session.order.quantity =
                Number(
                    $session.quantityInput
                );


            $global.updatePrices(
                $session.order
            );


        a: Количество: {{$session.order.quantity}}.
        go!: /ContinueOrder


    /*
     * ============================================================
     * АДРЕС
     * ============================================================
     */

    state: AskAddress

        InputText:
            prompt = Укажите полный адрес доставки. Например: ул. Ленина, д. 15, кв. 24
            varName = deliveryAddress
            then = /SaveAddress


    state: SaveAddress

        script:

            var address =
                String(
                    $session.deliveryAddress || ""
                ).trim();


            if (address.length < 8) {

                $reactions.answer(
                    "Похоже, адрес слишком короткий."
                );

                $reactions.transition(
                    "/AskAddress"
                );
            }

            else {

                $session.order.address =
                    address;

                $reactions.answer(
                    "Адрес доставки сохранён."
                );

                $reactions.transition(
                    "/ContinueOrder"
                );
            }


    /*
     * ============================================================
     * ТЕЛЕФОН
     * ============================================================
     */

    state: AskPhone

        InputPhoneNumber:
            prompt = Укажите номер телефона для связи с курьером. Например: +7 983 694-88-38
            varName = deliveryPhone
            failureMessage = ["Номер выглядит некорректно. Попробуйте ещё раз.", "Укажите корректный российский номер телефона."]
            then = /SavePhone


    state: SavePhone

        script:

            $session.order.phone =
                $session.deliveryPhone;


        a: Номер телефона сохранён.
        go!: /ContinueOrder


    /*
     * ============================================================
     * ВРЕМЯ ДОСТАВКИ
     * ============================================================
     *
     * Критическое исправление:
     * /SetDeliveryTime используется только здесь.
     */
    state: AskDeliveryTime

        a: Когда доставить заказ?
        a: Например: «к 18:00», «через час» или «сегодня вечером».

        go!: /AskDeliveryTime/Receive


        state: Receive

            intent: /SetDeliveryTime

            script:

                var timeValue =
                    $global.getSlotValue(
                        $parseTree,
                        "DeliveryTime"
                    );


                if ($global.hasValue(timeValue)) {

                    $session.order.deliveryTime =
                        $global.valueToText(
                            timeValue
                        );
                }


            a: Время доставки сохранено.
            go!: /ContinueOrder


            state: NoMatch
                event: noMatch

                a: Укажите время, например: «к 18:00», «через час» или «сегодня вечером».
                go!: /AskDeliveryTime/Receive


    /*
     * ============================================================
     * ОПЛАТА
     * ============================================================
     */

    state: AskPaymentMethod

        a: Как будете оплачивать заказ?
        a: Можно наличными или картой.

        go!: /AskPaymentMethod/Receive


        state: Receive

            intent: /SetPaymentMethod

            script:

                var paymentValue =
                    $global.getSlotValue(
                        $parseTree,
                        "PaymentMethod"
                    );


                if ($global.hasValue(paymentValue)) {

                    $session.order.paymentMethod =
                        $global.valueToText(
                            paymentValue
                        );
                }


            a: Способ оплаты сохранён.
            go!: /ContinueOrder


            state: NoMatch
                event: noMatch

                a: Выберите способ оплаты: наличными или картой.
                go!: /AskPaymentMethod/Receive


    /*
     * ============================================================
     * ПРОСМОТР ЗАКАЗА
     * ============================================================
     */

    state: ShowOrder
        intent!: /ShowOrder

        if: $session.order
            a: {{$global.getOrderText($session.order)}}
        else:
            a: Сейчас активного заказа нет.


    /*
     * ============================================================
     * ПРОВЕРКА ЗАКАЗА
     * ============================================================
     */

    state: CheckOrder

        script:

            $global.updatePrices(
                $session.order
            );


        a: {{$global.getOrderText($session.order)}}
        a: Всё верно? Ответьте «да» для оформления или «нет», чтобы изменить заказ.

        go!: /CheckOrder/Receive


        state: Receive

            state: Yes
                intent: /ConfirmOrder

                a: Заказ подтверждён.
                go!: /CompleteOrder


            state: No
                intent: /RejectOrder

                a: Хорошо. Что хотите изменить?
                a: Например: размер, основу, начинку или остроту.

                go!: /ChangeOrder/Wait


            state: NoMatch
                event: noMatch

                a: Ответьте «да», если всё верно, или «нет», если хотите что-то изменить.
                go!: /CheckOrder/Receive


    /*
     * ============================================================
     * ОФОРМЛЕНИЕ
     * ============================================================
     */

    state: CompleteOrder

        script:

            $global.updatePrices(
                $session.order
            );


        a: ✅ Заказ оформлен!
        a: {{$global.getOrderText($session.order)}}
        a: Спасибо за заказ! Курьер доставит его по указанному адресу.

        script:
            $analytics.setSessionResult(
                "Заказ оформлен"
            );

            $analytics.setScenarioAction(
                "Pizza order",
                JSON.stringify($session.order)
            );

            $jsapi.stopSession();


    /*
     * ============================================================
     * ОТМЕНА
     * ============================================================
     */

    state: CancelOrder
        intent!: /CancelOrder

        a: Заказ отменён.
        a: Все данные текущего заказа будут очищены.

        script:
            $jsapi.stopSession();


    /*
     * ============================================================
     * ОБЩАЯ ПОМОЩЬ
     * ============================================================
     */

    state: Help
        intent!: /Help

        a: Я могу помочь оформить заказ пиццы.
        a: Можно указать сразу несколько параметров, например:
        a: «Хочу большую острую Американу на тонком тесте с пепперони».
        a: Можно также написать «покажи заказ», «измени заказ» или «отмени заказ».


    /*
     * ============================================================
     * ГЛОБАЛЬНЫЙ НЕРАСПОЗНАННЫЙ ЗАПРОС
     * ============================================================
     */

    state: GlobalNoMatch
        event!: noMatch

        a: Не совсем понял вас.
        a: Можно написать, например: «Хочу большую Американу на тонком тесте с пепперони».