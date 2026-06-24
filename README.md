# Домашнее задание 4: Hilt + Room поверх проекта из ДЗ №3

### ФИО: Полатинский Артем
### Группа: ПИКД(2)

---

## Выбранный API: Rick and Morty API
Описание: Для проекта был выбран Rick and Morty API. Это RESTful API, которое предоставляет информацию о персонажах мультсериала. В приложении реализовано получение списка героев, поиск по имени и детальный просмотр информации.

## Что нового в ДЗ №4
1. **Dagger Hilt**: Полная миграция на Dependency Injection. Все зависимости (Retrofit, OkHttpClient, AppDatabase, Dao, Repository) предоставляются через Hilt-модули ([NetworkModule.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/di/NetworkModule.kt), [DatabaseModule.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/di/DatabaseModule.kt)), а не создаются вручную.
2. **Room Database**: Реализовано хранение избранных персонажей в БД Room (таблица `favourites`). Данные сохраняются локально и переживают перезапуск приложения.
3. **Архитектура Domain-Driven Design (DDD)**:
   * Выделен чистый слой `domain` (бизнес-логика, модели [Character.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/domain/model/Character.kt)/[Location.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/domain/model/Location.kt) и Use Cases) без Android-зависимостей и библиотек сериализации JSON (Gson).
   * Выделен слой данных `data` с сетевыми DTO-моделями ([CharacterDto.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/data/model/CharacterDto.kt)), Room-сущностями и реализацией репозитория [RickAndMortyRepositoryImpl.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/data/repository/RickAndMortyRepositoryImpl.kt), которая связывается через `@Binds` в Hilt-модуле [RepositoryModule.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/di/RepositoryModule.kt).
4. **Реактивная архитектура**: ViewModels переписаны на реактивные цепочки с использованием Flow-операторов (`combine`, `debounce`, `flatMapLatest`, `stateIn`). Все изменения избранного обновляются реактивно через Room Flow.
5. **Разделение ViewModel**: Каждому экрану выделена собственная специализированная ViewModel ([ListViewModel.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/ui/ListViewModel.kt), [DetailViewModel.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/ui/DetailViewModel.kt), [FavoritesViewModel.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/ui/FavoritesViewModel.kt)), что решает проблему переиспользования старого стейта при навигации.
6. **Безопасность и оптимизация**:
   * **Conditional HTTP Logging (Безопасность сетевого слоя)**: в [NetworkModule.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/di/NetworkModule.kt) логирование сетевых запросов `HttpLoggingInterceptor.Level.BODY` теперь активно только для отладочных сборок (`BuildConfig.DEBUG`), в релизных сборках логирование полностью отключается.
   * **Защита кода R8/ProGuard**: в сборке `release` включен R8 (`isMinifyEnabled = true`), а сетевые DTO-модели помечены аннотацией `@Keep` для защиты от обфускации.
   * **Сетевая безопасность**: подключена конфигурация [network_security_config.xml](file:///D:/Android_Polat/app/src/main/res/xml/network_security_config.xml) с запретом на cleartext traffic (HTTP-соединения).
   * **Динамический debounce / Оптимизация реактивных цепочек**: в [ListViewModel.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/ui/ListViewModel.kt) применен адаптивный debounce (`0L` для пустых запросов и первой загрузки, `500L` для ввода текста), что убрало искусственную задержку в полсекунды при первом запуске приложения.
   * **Надежность операций / Отказоустойчивость Room**: в [DetailViewModel.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/ui/DetailViewModel.kt) вызов добавления/удаления из избранного обернут в безопасный блок `try-catch` с логированием возможных сбоев Room.
7. **Оптимизация UI и кодовая гигиена**:
   * **Локализация и устранение хардкода**: все захардкоженные заголовки, пустые состояния и форматируемые шаблоны деталей персонажа в Compose-экранах ([ListScreen.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/ui/ListScreen.kt), [DetailScreen.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/ui/DetailScreen.kt), [FavoritesScreen.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/ui/FavoritesScreen.kt)) вынесены в строковые ресурсы [strings.xml](file:///D:/Android_Polat/app/src/main/res/values/strings.xml) и применены через `stringResource()`.
   * Все жестко заданные размеры (отступы, размеры аватарок и баннеров) вынесены из верстки в ресурсы размеров [dimens.xml](file:///D:/Android_Polat/app/src/main/res/values/dimens.xml) и применены через `dimensionResource()`.
   * Для всех ключевых экранов и компонентов добавлены Compose `@Preview` с тестовыми данными.
   * В списках `LazyColumn` добавлены уникальные ключи (`key = { it.id }`), а кнопке добавления в избранное задано описание `contentDescription` для поддержки TalkBack.
   * **Очистка импортов**: из [MainActivity.kt](file:///D:/Android_Polat/app/src/main/java/com/example/apiapp/MainActivity.kt) удален лишний неиспользуемый импорт `collectAsState`.

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
