# Домашнее задание 4: Hilt + Room поверх проекта из ДЗ №3

### ФИО: Полатинский Артем
### Группа: ПИКД(2)

---

## Выбранный API: Rick and Morty API
Описание: Для проекта был выбран Rick and Morty API. Это RESTful API, которое предоставляет информацию о персонажах мультсериала. В приложении реализовано получение списка героев, поиск по имени и детальный просмотр информации.

## Что нового в ДЗ №4
1. **Внедрение зависимостей (Hilt)**: Настроил автоматическую передачу нужных объектов (базы данных, сетевого клиента, репозиториев) через Hilt, а не создавал их вручную в коде. Модули: [NetworkModule.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/di/NetworkModule.kt), [DatabaseModule.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/di/DatabaseModule.kt).
2. **База данных Room**: Сделал сохранение избранных персонажей на устройстве в таблице `favourites`. При перезапуске приложения список избранного не стирается.
3. **Разделение проекта на слои**:
   * В слой `domain` вынес чистые модели героев ([Character.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/domain/model/Character.kt)/[Location.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/domain/model/Location.kt)) и логику (Use Cases). В этом слое нет кода от Android и библиотек сериализации JSON.
   * В слой `data` поместил сетевые модели DTO ([CharacterDto.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/data/model/CharacterDto.kt)), таблицы БД и саму реализацию репозитория ([RickAndMortyRepositoryImpl.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/data/repository/RickAndMortyRepositoryImpl.kt)).
4. **Реактивный интерфейс**: Все экраны обновляются с помощью Kotlin Flow. Любое изменение избранного в базе данных сразу же отображается на экране.
5. **Отдельные ViewModel для каждого экрана**: Разделил логику на три отдельные ViewModel ([ListViewModel.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/ui/ListViewModel.kt), [DetailViewModel.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/ui/DetailViewModel.kt), [FavoritesViewModel.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/ui/FavoritesViewModel.kt)), чтобы избежать багов с отображением старых данных при переходах.
6. **Оптимизации и безопасность**:
   * Настроил логирование сетевых запросов так, чтобы они писались в лог только при отладке (в дебаге), а в релизной версии приложения отключались.
   * Включил сжатие кода R8 для релизной версии и добавил аннотации `@Keep` для сетевых моделей, чтобы сборка не ломалась.
   * Отключил незащищенные HTTP-запросы в [network_security_config.xml](file:///D:/Android_Polat/app/src/main/res/xml/network_security_config.xml).
   * Убрал полусекундную задержку при первом запуске приложения — теперь задержка поиска срабатывает только при вводе текста, а не при старте.
   * Добавил блок `try-catch` во ViewModel, чтобы приложение не вылетало при ошибках сохранения в базу данных.
7. **Работа с ресурсами и интерфейсом**:
   * Вынес все захардкоженные тексты, шаблоны и сообщения в строковые ресурсы [strings.xml](file:///D:/Android_Polat/app/src/main/res/values/strings.xml).
   * Вынес размеры и отступы элементов интерфейса в [dimens.xml](file:///D:/Android_Polat/app/src/main/res/values/dimens.xml).
   * Добавил `@Preview` для просмотра компонентов в Android Studio.
   * Добавил уникальные ключи для списков `LazyColumn` и удалил лишний импорт `collectAsState` в [MainActivity.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/MainActivity.kt).

## Хранение в Room
* **Таблица**: `favourites`
* **Сущность**: `CharacterEntity`
* **Поля**: `id` (PrimaryKey), `name`, `status`, `species`, `type`, `gender`, `image`, `originName`, `locationName`.

## Сценарий проверки работоспособности Room
1. Запустите приложение.
2. Найдите любого персонажа (например, "Rick Sanchez") и перейдите на его детальный экран.
3. Нажмите кнопку **"Add to Favorites"** (кнопка поменяется на "Remove from Favorites", иконка сердца заполнится).
4. Закройте приложение (смахните его из панели запущенных процессов).
5. Запустите приложение снова и перейдите на экран **"Favorites"** (иконка сердца в верхнем меню).
6. Убедитесь, что добавленный персонаж отображается в списке избранного.
