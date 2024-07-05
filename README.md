# 🛠️Обмен вещами среди друзей
#### Шеринг как экономика совместного использования набирает сейчас всё большую полярность. Если в 2014 году глобальный рынок шеринга оценивался всего в $15 млрд, то к 2025 может достигнуть $335 млрд. 
#### Приложение обеспечивает пользователям, во-первых, возможность рассказывать, какими вещами они готовы поделиться, а во-вторых, находить нужную вещь и брать её в аренду на какое-то время. Сервис не только позволяет бронировать вещь на определённые даты, но и закрывает к ней доступ на время бронирования от других желающих. На случай, если нужной вещи на сервисе нет, у пользователей есть возможность оставлять запросы.
___

### Инструкция по развёртыванию/использованию
1. Склонируйте репозиторий:
   `git clone https://github.com/RomanBorodulin/java-shareit`
2. Перейдите в директорию проекта:
   `cd java-shareit`
3. Соберите проект и создайте Docker-образ:
   `mvn clean install`
4. Запустите приложение с использованием Docker Compose:
   `docker-compose up --build`
___
### Системные требования
* Java 11
* Apache Maven 3.6.0 или выше
* Docker
___
### Cтек технологий
* Java
* Spring Boot
* Maven
* Lombok
* PostgreSQL
* Hibernate
* Docker
___                                      
### API Reference
#### Вещи
* `POST /items` — добавление новой вещи.
* `PATCH /items/{itemId}` — редактирование вещи. Изменить можно название, описание и статус доступа к аренде. Редактировать вещь может только её владелец.
* `GET /items/{itemId}` — просмотр информации о конкретной вещи по её идентификатору. Информацию о вещи может просмотреть любой пользователь.
* `GET /items` — просмотр владельцем списка всех его вещей с указанием названия и описания для каждой.
* `GET /items/search?text={text}` — поиск вещи потенциальным арендатором. Пользователь передаёт в строке запроса текст, и система ищет вещи, содержащие этот текст в названии или описании. Поиск возвращает только доступные для аренды вещи.
___
#### Бронирование
* `POST /bookings` — добавление нового запроса на бронирование. Запрос может быть создан любым пользователем, а затем подтверждён владельцем вещи. После создания запрос находится в статусе WAITING — «ожидает подтверждения».
* `PATCH /bookings/{bookingId}?approved={approved}` — подтверждение или отклонение запроса на бронирование. Может быть выполнено только владельцем вещи. Затем статус бронирования становится либо APPROVED, либо REJECTED. Параметр approved может принимать значения true или false.
* `GET /bookings/{bookingId}` — получение данных о конкретном бронировании (включая его статус). Может быть выполнено либо автором бронирования, либо владельцем вещи, к которой относится бронирование.
* `GET /bookings?state={state}` — получение списка всех бронирований текущего пользователя. Параметр state необязательный и по умолчанию равен ALL (англ. «все»). Также он может принимать значения CURRENT (англ. «текущие»), PAST (англ. «завершённые»), FUTURE (англ. «будущие»), WAITING (англ. «ожидающие подтверждения»), REJECTED (англ. «отклонённые»). Бронирования возвращаются отсортированными по дате от более новых к более старым.
* `GET /bookings/owner?state={state}` — получение списка бронирований для всех вещей текущего пользователя. Этот запрос имеет смысл для владельца хотя бы одной вещи. Работа параметра state аналогична его работе в предыдущем сценарии.
___
#### Комментарии
* `POST /items/{itemId}/comment` — добавление нового комментария. Добавлять комментарий может пользователь тот который брал вещь в аренду.
* `GET /items/{itemId}` — прочитать комментарии для одной конкретной вещи.
* `GET /items` — комментарии для всех вещей данного пользователя.
___
#### Запросы
* `POST /requests` — добавить новый запрос вещи. Основная часть запроса — текст запроса, где пользователь описывает, какая именно вещь ему нужна.
* `GET /requests` — получить список своих запросов вместе с данными об ответах на них. Для каждого запроса должны указываться описание, дата и время создания и список ответов в формате: id вещи, название, id владельца. Запросы возвращаться в отсортированном порядке от более новых к более старым.
* `GET /requests/all?from={from}&size={size}` — получить список запросов, созданных другими пользователями. С помощью этого эндпоинта пользователи смогут просматривать существующие запросы, на которые они могли бы ответить. Запросы сортируются по дате создания: от более новых к более старым. Результаты возвращаются постранично. Для этого нужно передать два параметра: from — индекс первого элемента, начиная с 0, и size — количество элементов для отображения.
* `GET /requests/{requestId}` — получить данные об одном конкретном запросе вместе с данными об ответах на него. Посмотреть данные об отдельном запросе может любой пользователь.
