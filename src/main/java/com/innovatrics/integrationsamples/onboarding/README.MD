### Customer Onboarding
Customer onboarding serves for creating a digital identity of an individual called a customer. 
Onboarding aggregates all the data supplied by the client like selfies or document images 
which can be later used for inspecting the customer. This data is processed, the face is detected on the selfie, 
the document is classified, normalized and OCR is performed. Data is structured for straightforward inspection of the customer. 
Onboarding flow is customizable and only portion of the data can be supplied meeting the integrator's needs.

Following operations can be performed on the customer's data

Sample flow is shown in the sequence diagram below

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

### Customer Onboarding Workflow with MagnifEye Liveness
This sample shows alternative Customer Onboarding workflow. It follows the same concept as the workflow described above,
except it evaluates MagnifEye Liveness instead of Passive Liveness and the creation of customer selfie is performed later in the workflow.

When the customer is created, we create and evaluate MagnifEye liveness. Afterwards, the creation of customer selfie is performed by referencing the liveness selfie from the MagnifEye Liveness.
As the creation of MagnifEye Liveness record is performed by submitting a binary file produced by DOT components, it provides more security and ensures the selfies are produced by our components.

The sample workflow is shown in the diagram below

```mermaid
sequenceDiagram
    participant Client
    participant DIS

    Client->>+DIS: Create customer
    DIS-->>-Client: Customer ID
    par MagnifEye liveness
    Client->>+DIS: Create customer's liveness
    DIS-->>-Client: 
    Client->>+DIS: Create MagnifEye liveness record
    DIS-->>-Client: Liveness record selfie link
    Client->>+DIS: Evaluate MagnifEye liveness
    DIS-->>-Client: MagnifEye liveness result
    and selfie
    Client->>+DIS: Create customer's selfie referencing MagnifEye liveness selfie
    DIS-->>-Client: 
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

### Customer Onboarding Workflow with request sessions
This sample demonstrates Customer Onboarding workflow using request sessions, which ensure the provided data were created at the time of the onboarding.

Before the customer is created, session must be initialized, returning unique session token in the response. To create a customer linked to the session, the session token must be provided in the header.
Every subsequent request accessing this customer must have the same session token present in the header. 

While the onboarding flow is fully customizable, it is recommended that the submission of selfie, document pages and liveness data be in the form of binary files produced by DOT components to ensure the highest level of protection.
At the end of the onboarding process, the session is deleted, consequently deleting the linked customer as well.

The sample workflow is shown in the diagram below

```mermaid
sequenceDiagram
    participant Client
    participant DIS

    Client->>+DIS: Create session
    DIS-->>-Client: Session token
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
    Client->>DIS: Delete session
```