# ✈️ Lab2 FSI — Sistema de Reservas de Vuelo con Drools

Aplicación web y REST desarrollada en **Spring Boot** que aplica un conjunto de **10 reglas de negocio escritas en Drools (`.drl`)** para evaluar solicitudes de reserva de una aerolínea: ascensos de clase, descuentos, compensaciones por retrasos, asignación de asientos, equipaje y beneficios de lealtad.

Adicionalmente, puede generar una **explicación en lenguaje natural de la decisión** usando **Spring AI (OpenAI)**.

---

## 📑 Índice

- [Descripción](#-descripción)
- [Tecnologías](#-tecnologías)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Requisitos previos](#-requisitos-previos)
- [Instalación y ejecución](#-instalación-y-ejecución)
- [Configuración](#-configuración)
- [Endpoints](#-endpoints)
- [Ejemplo de uso (API REST)](#-ejemplo-de-uso-api-rest)
- [Las 10 reglas de negocio](#-las-10-reglas-de-negocio)
- [Modelo de datos](#-modelo-de-datos)
- [Pruebas](#-pruebas)
- [Estructura de una regla en Drools](#-estructura-de-una-regla-en-drools)

---

## 📖 Descripción

El sistema recibe una **solicitud de reserva (`BookingRequest`)** que contiene el pasajero, el vuelo, el equipaje, el precio del boleto y (opcionalmente) la lista de asientos disponibles.

El motor de reglas **Drools** evalúa los hechos (*facts*) contra las 10 reglas definidas en `src/main/resources/rules/airline_rules.drl` y produce una **respuesta (`BookingResponse`)** con:

- 📝 Mensajes detallados de cada acción disparada
- 💰 Descuento y precio final cuando aplica
- 🎁 Beneficios aplicados al pasajero (ascensos, puntos, salón VIP, asiento asignado, compensación, etc.)

### Flujo general

```
┌──────────────────┐      ┌──────────────────────────┐      ┌─────────────────────┐
│  BookingRequest  │ ───► │  BookingEvaluationService │ ───► │   BookingResponse   │
│  (hechos/facts)  │      │   + KieSession (Drools)   │      │  (mensajes/precios)  │
└──────────────────┘      └──────────────────────────┘      └─────────────────────┘
                                   │
                                   ▼ (opcional)
                          ┌──────────────────────┐
                          │  Spring AI (OpenAI)   │  → explicación en lenguaje natural
                          └──────────────────────┘
```

---

## 🛠 Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje de programación |
| Spring Boot | 3.2.5 | Framework de la aplicación |
| Drools (drools-core, drools-compiler, drools-mvel, drools-decisiontables) | 7.74.1.Final | Motor de reglas de negocio |
| KIE API | 7.74.1.Final | API de Drools |
| Thymeleaf | — | Plantillas HTML (formularios web) |
| Spring AI (OpenAI Starter) | 1.0.0-M6 | Explicaciones con IA (GPT-4o mini) |
| Spring Validation | — | Validación de datos de entrada (Jakarta Bean Validation) |
| Spring DevTools | — | Recarga en caliente durante el desarrollo |
| Maven Wrapper | — | Build sin necesidad de Maven instalado |

---

## 📂 Estructura del proyecto

```
labdrools/
├── pom.xml
├── mvnw / mvnw.cmd                  # Maven Wrapper
├── src/
│   ├── main/
│   │   ├── java/com/udea/labdrools/
│   │   │   ├── LabdroolsApplication.java        # Punto de entrada
│   │   │   ├── ai/
│   │   │   │   └── AIExplanationService.java    # Explicación de la decisión con IA
│   │   │   ├── config/
│   │   │   │   └── DroolsConfig.java            # Configuración de la sesión Drools
│   │   │   ├── controller/
│   │   │   │   ├── BookingEvaluationController.java  # Endpoints REST
│   │   │   │   └── BookingWebController.java         # Formulario web (Thymeleaf)
│   │   │   ├── model/
│   │   │   │   ├── BookingRequest.java          # Hechos de entrada
│   │   │   │   ├── BookingResponse.java         # Resultado de la evaluación
│   │   │   │   ├── Passenger.java
│   │   │   │   ├── Flight.java
│   │   │   │   ├── Luggage.java
│   │   │   │   └── Seat.java
│   │   │   └── service/
│   │   │       └── BookingEvaluationService.java  # Inserta hechos y dispara reglas
│   │   └── resources/
│   │       ├── application.yaml                 # Configuración
│   │       ├── rules/airline_rules.drl          # ⭐ Las 10 reglas de negocio
│   │       └── templates/
│   │           ├── booking_form.html            # Formulario de reserva
│   │           └── booking_result.html          # Página de resultados
│   └── test/java/com/udea/labdrools/            # Pruebas unitarias y de integración
└── target/                                      # Build (ignorado por Git)
```

---

## ✅ Requisitos previos

- **JDK 17 o superior** → [Descargar](https://adoptium.net/)
- **Git** (opcional)
- No es necesario instalar Maven: se usa el *Maven Wrapper* incluido (`./mvnw`)
- **Clave de API de OpenAI** (solo si deseas usar el endpoint con explicación por IA)

Verifica la versión de Java:

```bash
java -version
```

---

## 🚀 Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/AlejandroRuizG/Lab2fsi.git
cd Lab2fsi
```

### 2. Ejecutar la aplicación

**En Linux / macOS:**
```bash
./mvnw spring-boot:run
```

**En Windows (PowerShell / CMD):**
```powershell
.\mvnw.cmd spring-boot:run
```

### 3. Abrir en el navegador

```
http://localhost:8080/booking/form
```

Verás el formulario web para ingresar los datos del pasajero, vuelo y equipaje. Al enviarlo, Drools evalúa las reglas y se muestra la página de resultados con todos los beneficios aplicados.

> ℹ️ El servidor queda corriendo en el puerto **8080** con *LiveReload* activo (puerto 35729): si modificas una regla `.drl` o una clase, la aplicación se reinicia automáticamente.

### Compilar / empaquetar

```bash
./mvnw clean package     # genera target/labdrools-0.0.1-SNAPSHOT.jar
java -jar target/labdrools-0.0.1-SNAPSHOT.jar
```

---

## ⚙️ Configuración

La configuración se encuentra en `src/main/resources/application.yaml`:

```yaml
spring:
  application:
    name: labdrools
  profiles:
    active: local                 # carga application-local.yaml si existe
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:} # clave de OpenAI (fallback si no hay perfil local)
      chat:
        options:
          model: gpt-4o-mini      # modelo usado para las explicaciones
```

La clave de API **nunca se versiona**. Hay dos formas de configurarla:

**Opción A — Archivo local (recomendada en tu máquina de desarrollo)**

Crea `src/main/resources/application-local.yaml` (ya está en `.gitignore`, Git lo ignora):

```yaml
spring:
  ai:
    openai:
      api-key: sk-...
```

**Opción B — Variable de entorno (recomendada en CI/otras máquinas)**

```powershell
# Windows PowerShell
$env:OPENAI_API_KEY = "sk-..."
./mvnw spring-boot:run
```
```bash
# Linux / macOS
export OPENAI_API_KEY="sk-..."
./mvnw spring-boot:run
```

Si no existe ninguno de los dos, la app arranca igualmente: solo el endpoint con IA devolverá un mensaje de respaldo.

> El endpoint `/booking/api/evaluate` y el formulario web **funcionan sin clave de OpenAI**; solo `/booking/evaluateBooking` requiere IA.

---

## 🔌 Endpoints

| Método | Ruta | Descripción | Requiere IA |
|---|---|---|---|
| `GET` | `/booking/form` | Formulario web (Thymeleaf) | ❌ |
| `POST` | `/booking/evaluate` | Evaluación desde el formulario web → HTML de resultados | ❌ |
| `POST` | `/booking/api/evaluate` | Evaluación con Drools vía JSON (con `@Valid`) | ❌ |
| `POST` | `/booking/evaluateBooking` | Evaluación con Drools + explicación con IA | ✅ |

---

## 💻 Ejemplo de uso (API REST)

### Petición

```bash
curl -X POST http://localhost:8080/booking/api/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "passenger": {
      "name": "María Gómez",
      "age": 67,
      "membershipStatus": "Gold",
      "seatPreference": "Any",
      "travelingWithChildren": false,
      "loyaltyPoints": 1200
    },
    "flight": {
      "flightNumber": "LA1234",
      "delayMinutes": 200,
      "durationHours": 6.5
    },
    "luggage": {
      "tag": "MAleta01",
      "weightKg": 8
    },
    "ticketPrice": 500000,
    "availableSeats": [
      { "seatNumber": "12A", "seatType": "EMERGENCY_EXIT" },
      { "seatNumber": "14C", "seatType": "FAMILY" },
      { "seatNumber": "20B", "seatType": "STANDARD" }
    ]
  }'
```

### Respuesta esperada

```json
{
  "messages": [
    "Ascenso a clase ejecutiva otorgado: pasajero con estatus Gold y retraso de 200 minutos.",
    "Check-in prioritario activado: pasajero mayor de 65 años (67 años).",
    "Descuento del 10% aplicado: equipaje de 8.0 kg.",
    "Asiento de salida de emergencia 12A asignado y marcado como ocupado.",
    "Compensación de $200 otorgada: retraso de 200 minutos (mayor a 3 horas).",
    "500 puntos de lealtad agregados: membresía Gold en vuelo de 6.5 horas."
  ],
  "discountPercent": 10.0,
  "finalPrice": 450000.0
}
```

> En este ejemplo se disparan **6 de las 10 reglas**: reglas 1, 2, 3, 5, 6 y 7.

---

## 📜 Las 10 reglas de negocio

Definidas en `src/main/resources/rules/airline_rules.drl`:

| # | Nombre de la regla | Condición (`when`) | Acción (`then`) |
|---|---|---|---|
| 1 | `UpgradeToBusinessClassForFrequentFlyersWithDelays` | Membresía **Gold/Platinum** y retraso **> 60 min** | Ascenso a clase ejecutiva |
| 2 | `PriorityCheckInForSeniors` | Edad **> 65 años** | Activa check-in prioritario |
| 3 | `DiscountForLightLuggage` | Equipaje **< 10 kg** | **10 % de descuento** sobre el precio del boleto |
| 4 | `DenyUpgradeForOverweightLuggage` | Equipaje **> 23 kg** | Marca al pasajero como **no elegible** para ascensos |
| 5 | `AssignEmergencyExitSeatToYoungAdults` | Edad **18–40**, preferencia `"Any"` y hay asiento `EMERGENCY_EXIT` libre | Asigna el asiento y lo marca como ocupado |
| 6 | `CompensationForExtremeDelays` | Retraso **> 180 min (3 h)** | Otorga compensación de **$200** |
| 7 | `ExtraLoyaltyPointsForLongFlights` | Membresía ≠ `Basic` y vuelo **> 5 h** | Suma **500 puntos** de lealtad |
| 8 | `RestrictLuggageOnShortFlights` | Equipaje **> 15 kg** y vuelo **< 2 h** | Marca el equipaje como **no permitido** |
| 9 | `VipLoungeAccessForPlatinumMembers` | Membresía **Platinum** | Otorga acceso al **salón VIP** |
| 10 | `PreferentialSeatForFamilies` | Viaja con niños y sin preferencia de asiento, hay asiento `FAMILY` libre | Asigna asiento preferencial para familias |

> 📌 Las reglas pueden dispararse **varias veces en la misma evaluación** (no son excluyentes entre sí).

---

## 🧩 Modelo de datos

### `BookingRequest` (entrada)

| Campo | Tipo | Descripción |
|---|---|---|
| `passenger` | `Passenger` | Datos y resultados del pasajero |
| `flight` | `Flight` | Datos del vuelo |
| `luggage` | `Luggage` | Equipaje |
| `ticketPrice` | `double` | Precio original del boleto |
| `availableSeats` | `List<Seat>` | Asientos libres (participan en las reglas 5 y 10) |

### `Passenger`

| Campo | Tipo | Descripción |
|---|---|---|
| `name` | `String` | Nombre (requerido, 2–100 caracteres) |
| `age` | `int` | Edad (0–120) |
| `membershipStatus` | `String` | `Basic` \| `Gold` \| `Platinum` |
| `seatPreference` | `String` | `Any` \| `Window` \| `Aisle` (o `null`) |
| `travelingWithChildren` | `boolean` | Viaja acompañado de niños |
| `upgradedToBusinessClass` | `boolean` | *Resultado* — ascenso a ejecutiva |
| `priorityCheckInEnabled` | `boolean` | *Resultado* — check-in prioritario |
| `eligibleForUpgrade` | `boolean` | *Resultado* — elegibilidad para ascensos (default `true`) |
| `vipLoungeAccess` | `boolean` | *Resultado* — acceso al salón VIP |
| `assignedSeat` | `String` | *Resultado* — asiento asignado |
| `compensationAmount` | `double` | *Resultado* — compensación monetaria |
| `loyaltyPoints` | `int` | *Resultado* — puntos de lealtad |

### `Flight`

| Campo | Tipo | Descripción |
|---|---|---|
| `flightNumber` | `String` | Número de vuelo (2–10 caracteres) |
| `delayMinutes` | `int` | Retraso en minutos |
| `durationHours` | `double` | Duración del vuelo en horas |

### `Luggage`

| Campo | Tipo | Descripción |
|---|---|---|
| `tag` | `String` | Etiqueta del equipaje |
| `weightKg` | `double` | Peso en kilogramos |
| `allowed` | `boolean` | *Resultado* — `false` si una regla lo restringe (default `true`) |

### `Seat`

| Campo | Tipo | Descripción |
|---|---|---|
| `seatNumber` | `String` | Número de asiento (máx. 5 caracteres) |
| `seatType` | `String` | `EMERGENCY_EXIT` \| `FAMILY` \| `STANDARD` |
| `occupied` | `boolean` | *Resultado* — `true` cuando una regla lo asigna |

### `BookingResponse` (salida)

| Campo | Tipo | Descripción |
|---|---|---|
| `messages` | `List<String>` | Mensajes de cada regla disparada |
| `discountPercent` | `double` | Porcentaje de descuento aplicado |
| `finalPrice` | `double` | Precio final con descuento |

---

## 🧪 Pruebas

Ejecuta la suite completa de pruebas:

```bash
./mvnw test
```

Pruebas incluidas:

| Clase | Cubre |
|---|---|
| `AirlineRulesTest` | Disparo individual de las 10 reglas de Drools |
| `BookingEvaluationServiceTest` | Lógica del servicio de evaluación |
| `BookingEvaluationControllerTest` | Endpoints REST (incluida la validación de entrada) |
| `LabdroolsApplicationTests` | Contexto de Spring Boot |

---

## 🧠 Estructura de una regla en Drools

```drl
// Comentario que describe la regla
rule "NombreDeLaRegla"
    when                         // ✅ CONDICIÓN: qué hechos deben coincidir
        $request : BookingRequest(passenger.age > 65)
        $response : BookingResponse()
    then                         // ⚡ ACCIÓN: qué hacer cuando coincide
        $request.getPassenger().setPriorityCheckInEnabled(true);
        $response.addMessage("Check-in prioritario activado.");
end
```

- **`when`**: patrón sobre los *facts* insertados en la sesión (`BookingRequest`, `BookingResponse`, `Seat`).
- **`then`**: bloque Java/MVEL que modifica los hechos y acumula mensajes en la respuesta.
- Las variables se declaran con `$nombre` por convención.

---

## 📚 Laboratorio

**Curso:** Fundamentos de Sistemas Inteligentes (FSI) — Universidad de Antioquia (UdeA)
**Tema:** Sistemas basados en reglas con Drools

---

## ✍️ Autor

**Alejandro Ruiz G.**
GitHub: [@AlejandroRuizG](https://github.com/AlejandroRuizG)
