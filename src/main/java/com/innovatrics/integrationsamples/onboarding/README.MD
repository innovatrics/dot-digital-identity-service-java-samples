### Customer Onboarding
Customer onboarding serves for creating a digital identity of an individual called a customer. Onboarding aggregates all the data supplied by the client like selfies or document images which can be later used for inspecting the customer. This data is processed, the face is detected on the selfie, the document is classified, normalized and OCR is performed. Data is structured for straightforward inspection of the customer. Onboarding flow is customizable and only portion of the data can be supplied meeting the integrator's needs.

Following operations can be performed on the customer's data

Example flow is shown in the sequence diagram below

```mermaid
sequenceDiagram
    participant Client
    participant DIS
    
    Client->>+DIS: Create customer
    DIS-->>-Client: Customer ID
    par selfie
    Client->>+DIS: Create customer's selfie
    DIS-->>-Client: 
    and passive liveness
    Client->>+DIS: Create customer's liveness
    DIS-->>-Client: 
    Client->>DIS: Create passive liveness selfie
    Client->>+DIS: Evaluate passive liveness
    DIS-->>-Client: Passive liveness result
    and document
    Client->>+DIS: Create customer's document
    DIS-->>-Client: 
    Client->>DIS: Create document front page
    Client->>DIS: Create document back page
    end
    Client->>+DIS: Get customer's info
    DIS-->>-Client: Customer's info
    par front page
    Client->>+DIS: Get document front page crop
    DIS-->>-Client: Front page crop image
    and back page
    Client->>+DIS: Get document back page crop
    DIS-->>-Client: Back page crop image
    and document portrait
    Client->>+DIS: Get document portrait
    DIS-->>-Client: Document portrait image
    end
    Client->>DIS: Delete customer   
```
