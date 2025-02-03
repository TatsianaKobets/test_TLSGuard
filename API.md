# API Documentation

## Endpoints

### 1. Add a new website URL with schedule

- **URL**: `/api/websites`
- **Method**: `POST`
- **Request Body**:

  ```json
  {
       "url": "https://example.com",
       "schedule": "hourly"
  }
  ```
- **Valid Schedule Values:** "minutely", "hourly", "daily", "weekly"
- **Response**:
  - **200 OK** (Success):
    ```json
    {
        "status": "ok"
    }
    ```
  - **400 Bad Request** (Invalid URL or duplicate):
    ```json
    {
        "error": "Invalid URL"
    }
    ```
     or
    ```json
    {
      "error": "Website already exists"
    }
### 2. Get all monitored websites

- **URL**: `/api/websites`
- **Method**: `GET`
- **Response**:

  - **200 OK**:
    ```json
    [
      {
        "id": 1,
        "url": "https://example.com",
        "schedule": "hourly",
        "last_checked": "2023-01-01T12:00:00Z",
        "valid_to": "2024-01-01T12:00:00Z"
      }
    ]
    ```
    Includes latest certificate validity information if available

### 3. Delete all websites and certificates

- **URL**: `/api/websites`
- **Method**: `DELETE`
- **Response**:
    - **200 OK**:
    ```json
    {
         "status": "ok"
    }
    ```

### 4. Internal Certificate Submission

- **URL**: `/api/certificates`
- **Usage:** Used internally by the scheduler to store certificate data. Not intended for direct external use.
- **Request Body** (example)::
    ```json
    {
      "website_id": 1,
      "pem": "-----BEGIN CERTIFICATE-----...-----END CERTIFICATE-----"
    }
    ```
 
## Key Notes
### Database Schema
Table: websites     |	Fields: id, url (unique), schedule, last_checked

Table: certificates |	Fields: id, website_id,   subject,  issuer,      valid_from, valid_to, pem

#### Automated Certificate Checks
- Certificates are validated every 1 minute based on the website's schedule.
- Validity checks include:
  * Expiration dates (valid_from and valid_to)
  * Certificate chain integrity
- Results are stored in the certificates table.

#### Error Handling
All errors return JSON with an error field:
    ```json
     { 
        "error": "Descriptive error message" 
     }
    ```
#### Common Status Codes:
- 400: Invalid input (e.g., malformed URL)
- 404: Endpoint not found
- 500: Server-side error (e.g., database failure)

#### CORS Configuration 

http
* Access-Control-Allow-Origin: *
* Access-Control-Allow-Methods: GET, POST, DELETE
* Access-Control-Allow-Headers: Content-Type
     

## Notes
- All responses are in JSON format.

#### Example Workflow
1. Add a Website:
bash
* curl -X POST http://localhost:8080/api/websites \
* -H "Content-Type: application/json" \
* -d '{"url":"https://google.com","schedule":"daily"}'
2. List All Websites:
bash
* curl http://localhost:8080/api/websites
3. Scheduler Automatically:
 - Checks certificates based on the schedule
 - Updates last_checked and valid_to fields
4. Monitor Expiry:
Use the valid_to timestamp from GET /api/websites to track certificate validity.