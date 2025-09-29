# OpenMRS Drools Module - Documentation Structure

## Table of Contents

### Part I: Getting Started

1. [Module Overview & Introduction](#1-module-overview--introduction)
2. [Installation & Basic Setup](#2-installation--basic-setup)
3. [Quick Start Guide - Your First Rule](#3-quick-start-guide---your-first-rule)

### Part II: Core Concepts

4. [Understanding Rules and Facts](#4-understanding-rules-and-facts)
5. [Drools Sessions Deep Dive](#5-drools-sessions-deep-dive)
6. [Knowledge Base Management](#6-knowledge-base-management)

### Part III: Configuration & Setup

7. [Rule Providers - Programmatic vs Declarative](#7-rule-providers---programmatic-vs-declarative)
8. [JSON Configuration Reference](#8-json-configuration-reference)
9. [Session Parameters and Types](#9-session-parameters-and-types)
10. [Global Properties and System Configuration](#10-global-properties-and-system-configuration)

### Part IV: Rule Authoring

11. [DRL (Drools Rule Language) Guide](#11-drl-drools-rule-language-guide)
12. [Decision Tables - Excel/CSV Rules](#12-decision-tables---excelcsv-rules)
13. [When to Use DRL vs Decision Tables](#13-when-to-use-drl-vs-decision-tables)
14. [Rule Best Practices and Patterns](#14-rule-best-practices-and-patterns)

### Part V: Advanced Features

15. [Session Management and Lifecycle](#15-session-management-and-lifecycle)
16. [Event-Driven Rules and Listeners](#16-event-driven-rules-and-listeners)
17. [Agenda Management and Control](#17-agenda-management-and-control)
18. [Fact Management - Insert vs Logical Insert](#18-fact-management---insert-vs-logical-insert)

### Part VI: Integration and APIs

19. [OpenMRS Integration Patterns](#19-openmrs-integration-patterns)
20. [Calculation Service and Utilities](#20-calculation-service-and-utilities)
21. [Global Bindings and Helper Functions](#21-global-bindings-and-helper-functions)
22. [REST API Reference](#22-rest-api-reference)

### Part VII: Testing and Debugging

23. [Testing Strategies for Rules](#23-testing-strategies-for-rules)
24. [Debugging and Troubleshooting](#24-debugging-and-troubleshooting)
25. [Performance Optimization](#25-performance-optimization)

### Part VIII: Production and Maintenance

26. [Deployment Best Practices](#26-deployment-best-practices)
27. [Monitoring and Logging](#27-monitoring-and-logging)
28. [Common Pitfalls and Solutions](#28-common-pitfalls-and-solutions)

---

## Detailed Content Structure

### 1. Module Overview & Introduction

#### What is Clinical Decision Support?

- Definition and importance in healthcare
- How CDS improves patient outcomes
- Types of clinical decisions supported

#### Why Drools for OpenMRS?

- Business rules engine benefits
- Phreak algorithm performance advantages
- Integration with OpenMRS architecture
- Community and enterprise support

#### Key Use Cases

- **Patient Flags and Alerts**
  - Sepsis detection
  - Drug allergy warnings
  - Preventive care reminders
- **Clinical Protocol Enforcement**
  - Treatment guidelines
  - Quality measures
  - Care pathways
- **Population Health Management**
  - Cohort identification
  - Risk stratification
  - Outcome tracking

#### Module Architecture Overview

- High-level component diagram
- Data flow from OpenMRS to rules engine
- Integration points and dependencies

---

### 2. Installation & Basic Setup

#### Prerequisites

- OpenMRS version compatibility
- System requirements
- Required modules and dependencies

#### Installation Steps

1. Module download and installation
2. Initial configuration
3. Verification steps
4. Common installation issues

#### Directory Structure

```
{openmrs_app_directory}/
└── drools/
    ├── provider1/
    │   ├── provider.json
    │   └── rules/
    │       ├── rule1.drl
    │       └── decision_table.xlsx
    └── provider2/
        ├── provider.json
        └── rules/
```

#### Global Properties Configuration

- `drools.storage_dir` - Default storage location
- Other relevant global properties
- Configuration validation

---

### 3. Quick Start Guide - Your First Rule

#### Simple Patient Flag Example

- Problem: Flag patients with high fever
- Step-by-step implementation
- Testing and verification

#### Basic provider.json

```json
{
  "isEnabled": true,
  "rules": [
    {
      "name": "High Fever Alert",
      "path": "rules/fever_alert.drl"
    }
  ],
  "sessionConfigs": [
    {
      "sessionId": "FeverCheck",
      "params": [
        {
          "name": "patient",
          "type": "PATIENT_UUID",
          "required": true
        }
      ]
    }
  ]
}
```

#### Simple DRL Rule

```drl
rule "High Fever Alert"
when
    Patient($id : uuid)
    Obs(person.uuid == $id,
        concept.uuid == "FEVER_CONCEPT",
        valueNumeric > 38.5)
then
    insert(new Alert("Patient has high fever: " + $fever));
end
```

---

### 4. Understanding Rules and Facts

#### What is a Rule?

- **When** clause (Left Hand Side - LHS)
  - Pattern matching
  - Constraints and conditions
  - Variable binding
- **Then** clause (Right Hand Side - RHS)
  - Actions to execute
  - Fact insertion/modification
  - External service calls

#### What is a Fact?

- Objects in working memory
- Types of facts in OpenMRS context:
  - **Patient** - Demographics, identifiers
  - **Obs** - Clinical observations
  - **Encounter** - Clinical interactions
  - **Visit** - Healthcare episodes
  - **Order** - Medical orders
  - **Custom objects** - Domain-specific data

#### Rule Evaluation Process

1. Pattern matching against facts
2. Constraint evaluation
3. Rule activation
4. Conflict resolution
5. Action execution
6. Fact modification triggers re-evaluation

#### Working Memory Concept

- Fact insertion and retraction
- Memory management
- Performance considerations

---

### 5. Drools Sessions Deep Dive

#### Session Types

##### Stateless Sessions

- **Characteristics:**

  - Created on demand
  - No memory between invocations
  - Disposed after single use
  - Thread-safe by design

- **When to Use:**

  - Simple evaluations
  - One-time rule execution
  - Stateless calculations
  - High-throughput scenarios

- **Example Use Cases:**
  - Drug interaction checking
  - Simple alerts
  - Validation rules

##### Stateful Sessions

- **Characteristics:**

  - Maintain state between rule firings
  - Support for logical insertions
  - Event-driven capabilities
  - Require manual lifecycle management

- **When to Use:**

  - Complex workflows
  - Event processing
  - Multi-step decisions
  - Long-running processes

- **Example Use Cases:**
  - Care pathway management
  - Complex clinical protocols
  - Continuous monitoring

#### Session Lifecycle Management

##### On-Demand Sessions (Default)

1. Configuration loaded
2. Parameters resolved
3. Session created and hydrated
4. Rules executed
5. Results extracted
6. Session disposed

##### Auto-Start Sessions

- Created when module starts
- Disposed when module stops
- Used with event listeners
- Ideal for reactive systems

#### Session Configuration Attributes

- **sessionId** - Unique identifier
- **globals** - Global variable bindings
- **params** - Input parameters
- **returnObjectsTypeClassName** - Result type filtering
- **agendaGroup** - Rule grouping
- **agendaFilter** - Rule filtering
- **systemEventListeners** - OpenMRS event handlers
- **sessionRuntimeEventListeners** - Drools event handlers

---

### 6. Knowledge Base Management

#### Knowledge Base Components

- **Rules Repository**

  - DRL files
  - Decision tables
  - Rule templates

- **Facts Schema**

  - Domain model
  - Data types
  - Relationships

- **Configuration Metadata**
  - Provider definitions
  - Session configurations
  - Parameter mappings

#### Compilation Process

1. Rule parsing and validation
2. Pattern analysis and optimization
3. Network generation (Rete/Phreak)
4. Knowledge base building
5. Error handling and reporting

#### Version Management

- Rule versioning strategies
- Backward compatibility
- Migration procedures
- Rollback mechanisms

---

### 7. Rule Providers - Programmatic vs Declarative

#### Programmatic Providers

##### RuleProvider Interface

```java
public interface RuleProvider {
    void configure(RuleProviderConfig config);
    Collection<String> getRuleDefinitions();
    Collection<SessionConfig> getSessionConfigs();
    // Additional methods...
}
```

##### Implementation Benefits

- Custom agenda filters
- Dynamic rule generation
- Complex global bindings
- Custom event listeners
- Advanced session configuration

##### When to Use Programmatic

- Complex business logic
- Dynamic rule generation
- Custom integration requirements
- Advanced filtering needs

#### Declarative Providers (JSON)

##### Advantages

- Simple configuration
- No coding required
- Easy maintenance
- Quick setup

##### Limitations

- Limited customization
- No dynamic behavior
- Standard agenda filters only
- Basic global bindings

##### File Structure Requirements

```
provider_directory/
├── provider.json        # Required
├── rules/
│   ├── rule1.drl       # Optional
│   ├── table1.xlsx     # Optional
│   └── helpers.drl     # Optional
```

---

### 8. JSON Configuration Reference

#### Complete provider.json Schema

```json
{
  "isEnabled": true|false,
  "description": "Provider description",
  "version": "1.0.0",
  "rules": [
    {
      "name": "Unique rule name",
      "path": "relative/path/to/rule/file",
      "description": "Rule description"
    }
  ],
  "sessionConfigs": [
    {
      "sessionId": "UniqueSessionId",
      "description": "Session description",
      "agendaGroup": "groupName",
      "agendaFilter": "filterName",
      "params": [
        {
          "name": "parameterName",
          "type": "PARAMETER_TYPE",
          "required": true|false,
          "defaultValue": "default"
        }
      ],
      "returnObjectsTypeClassName": "full.class.Name",
      "globals": {
        "globalName": "globalValue"
      },
      "autoStart": true|false
    }
  ]
}
```

#### Configuration Validation

- Required fields checking
- Parameter type validation
- File path verification
- Circular dependency detection

#### Common Configuration Patterns

- Single rule, single session
- Multiple rules, shared session
- Rule families with different sessions
- Event-driven configurations

---

### 9. Session Parameters and Types

#### Supported Parameter Types

##### PATIENT_UUID

- **Description:** Patient identifier
- **Resolution:** Loads Patient object from database
- **Usage:** Most common parameter for patient-specific rules
- **Example:** `"type": "PATIENT_UUID"`

##### ENCOUNTER_UUID

- **Description:** Encounter identifier
- **Resolution:** Loads Encounter object with associated data
- **Usage:** Encounter-specific rules
- **Related Facts:** Patient, Visit, Obs, Orders

##### OBS_UUID

- **Description:** Observation identifier
- **Resolution:** Loads specific Obs object
- **Usage:** Observation-focused rules
- **Related Facts:** Patient, Encounter, Concept

##### VISIT_UUID

- **Description:** Visit identifier
- **Resolution:** Loads Visit with encounters and observations
- **Usage:** Visit-level analytics and rules
- **Related Facts:** Patient, Encounters, Obs

##### FORM_UUID

- **Description:** Form identifier
- **Resolution:** Loads Form definition
- **Usage:** Form-based validation and logic
- **Related Facts:** FormFields, Concepts

##### DRUG_UUID

- **Description:** Drug identifier
- **Resolution:** Loads Drug object
- **Usage:** Medication-related rules
- **Related Facts:** Concepts, Drug interactions

##### LITERAL

- **Description:** Static value parameter
- **Resolution:** Used as-is without database lookup
- **Usage:** Configuration values, thresholds
- **Example:** Dosage limits, age thresholds

#### Parameter Resolution Process

1. Parameter type identification
2. UUID validation
3. Database query execution
4. Object loading and hydration
5. Related object loading (lazy/eager)
6. Fact insertion into session

#### Required vs Optional Parameters

- **Required:** Must be provided, execution fails if missing
- **Optional:** Can be null, rules should handle gracefully
- **Default Values:** Used when optional parameter not provided

---

### 10. Global Properties and System Configuration

#### Module Global Properties

- **drools.storage_dir**

  - Default: "drools"
  - Location: OpenMRS app directory
  - Purpose: Rule provider storage location

- **drools.auto_reload**

  - Default: true (development), false (production)
  - Purpose: Automatic rule reloading on file changes

- **drools.performance_monitoring**
  - Default: false
  - Purpose: Enable detailed performance logging

#### System Integration Points

- OpenMRS Context and Services
- Event Module integration
- Calculation Module integration
- Reporting Module hooks

#### Security Considerations

- Rule execution permissions
- File system access controls
- Parameter injection security
- Output sanitization

---

### 11. DRL (Drools Rule Language) Guide

#### DRL File Structure

```drl
package com.example.rules;

import org.openmrs.Patient;
import org.openmrs.Obs;

global org.openmrs.module.drools.DroolsCalculationService service;

rule "Rule Name"
    dialect "mvel"
    agenda-group "groupName"
    salience 100
when
    // Conditions (Left Hand Side)
    $patient : Patient()
    $obs : Obs(person == $patient, concept.uuid == "CONCEPT_UUID")
then
    // Actions (Right Hand Side)
    insert(new Alert("Alert message"));
end
```

#### Pattern Matching

##### Basic Patterns

```drl
// Simple object matching
Patient($id : uuid)

// Property constraints
Obs(valueNumeric > 38.5)

// Multiple constraints
Obs(person.uuid == $patientId, concept.uuid == "FEVER", valueNumeric > 38.5)
```

##### Advanced Patterns

```drl
// Conditional elements
Patient($id : uuid) and
Obs(person.uuid == $id, concept.uuid == "DIABETES")

// Negation
Patient($id : uuid) and
not Obs(person.uuid == $id, concept.uuid == "ALLERGY_RECORDED")

// Existence check
Patient($id : uuid) and
exists Obs(person.uuid == $id, concept.uuid == "HIGH_RISK")
```

##### Collections and Accumulation

```drl
// Count observations
Patient($id : uuid)
$count : Number() from accumulate(
    Obs(person.uuid == $id, concept.uuid == "BP_SYSTOLIC"),
    count(1)
)
eval($count > 3)
```

#### Variable Binding and Usage

- **Binding Syntax:** `$variable : Pattern()`
- **Property Binding:** `Patient($name : givenName)`
- **Constraint Binding:** `Obs($value : valueNumeric > 140)`

#### Rule Attributes

- **salience** - Rule priority (higher fires first)
- **agenda-group** - Rule grouping
- **dialect** - Language (java/mvel)
- **no-loop** - Prevent infinite loops
- **lock-on-active** - Rule activation control

#### Functions and Utilities

```drl
function boolean isAdult(Patient patient) {
    return patient.getAge() >= 18;
}

rule "Adult Medication Rule"
when
    $patient : Patient()
    eval(isAdult($patient))
then
    // Adult-specific logic
end
```

---

### 12. Decision Tables - Excel/CSV Rules

#### Decision Table Structure

##### Column Types

- **CONDITION** - Rule conditions (When part)
- **ACTION** - Rule actions (Then part)
- **METADATA** - Rule attributes (salience, agenda-group)

##### Basic Table Layout

| RuleTable | Drug Allergy Checker      |                       |                                          |
| --------- | ------------------------- | --------------------- | ---------------------------------------- |
|           | CONDITION                 | CONDITION             | ACTION                                   |
|           | patient: Patient          | allergy: String       | action                                   |
|           | $patient.uuid == "$param" | drug.name == "$param" | insert(new Alert("Allergy: " + $param)); |
| Rule 1    | patient-uuid-1            | Penicillin            |                                          |
| Rule 2    | patient-uuid-2            | Aspirin               |                                          |

#### Table Configuration

```excel
RuleTable DrugAllergyRules
CONDITION,CONDITION,ACTION
patient: Patient,drug: Drug,
$patient.allergies contains $drug.name,,$1 == "$param",insert(new Alert("Drug allergy detected"));
Rule1,true,PENICILLIN,
Rule2,true,ASPIRIN,
```

#### Advanced Table Features

##### Parameter Substitution

- **$param** - Cell value substitution
- **$1, $2, $3** - Numbered parameters
- **${param}** - Named parameters

##### Template Support

```excel
CONDITION
patient: Patient($uuid: uuid == "$param{uuid}")
obs: Obs(person.uuid == $uuid, concept.uuid == "$param{concept}", valueNumeric $param{operator} $param{value})
```

#### Table Compilation Process

1. Excel/CSV parsing
2. Template processing
3. DRL generation
4. Rule compilation
5. Knowledge base integration

#### Validation and Testing

- Column header validation
- Data type checking
- Template syntax verification
- Rule conflict detection

---

### 13. When to Use DRL vs Decision Tables

#### Decision Tables Are Best For:

##### Multiple Similar Rules

```
IF Patient Age > 65 AND Medication = "Warfarin" THEN Alert = "High Risk"
IF Patient Age > 65 AND Medication = "Digoxin" THEN Alert = "High Risk"
IF Patient Age > 65 AND Medication = "NSAIDs" THEN Alert = "High Risk"
```

##### Business Rule Matrices

- Drug-drug interactions
- Age-based dosing guidelines
- Risk scoring algorithms
- Protocol decision trees

##### Clinician-Friendly Authoring

- Non-technical users
- Rapid prototyping
- Business rule changes
- Audit trail requirements

#### DRL Is Best For:

##### Complex Logic

```drl
rule "Complex Sepsis Detection"
when
    $patient : Patient()

    // Temperature criteria
    $temp : Obs(person == $patient, concept.uuid == "TEMPERATURE",
                valueNumeric > 38.0 || valueNumeric < 36.0)

    // Heart rate criteria
    $hr : Obs(person == $patient, concept.uuid == "HEART_RATE",
              valueNumeric > 90)

    // White blood cell count
    ($wbc : Obs(person == $patient, concept.uuid == "WBC",
               valueNumeric > 12000 || valueNumeric < 4000)
     or
     exists Obs(person == $patient, concept.uuid == "BANDS", valueNumeric > 10))

    // Confirm recent observations (within 24 hours)
    eval(isRecent($temp) && isRecent($hr) && isRecent($wbc))
then
    insert(new SepsisAlert($patient, "Possible sepsis - SIRS criteria met"));
end
```

##### Single Complex Rules

- Unique business logic
- Complex calculations
- Multi-step workflows
- Integration patterns

##### Advanced Features Needed

- Custom functions
- Complex accumulations
- Event processing
- Dynamic rule generation

#### Hybrid Approach

- Use tables for rule families
- Use DRL for complex individual rules
- Combine in single provider
- Share common functions and globals

---

### 14. Rule Best Practices and Patterns

#### Performance Best Practices

##### Efficient Pattern Matching

```drl
// Good - Most selective constraint first
Obs(concept.uuid == "SPECIFIC_CONCEPT", person.uuid == $patientId, valueNumeric > 100)

// Poor - Least selective constraint first
Obs(valueNumeric > 100, person.uuid == $patientId, concept.uuid == "SPECIFIC_CONCEPT")
```

##### Minimize Object Creation

```drl
// Good - Reuse existing objects
modify($existingAlert) { setSeverity("HIGH") }

// Poor - Create new objects unnecessarily
insert(new Alert($existingAlert.getMessage(), "HIGH"));
```

##### Use Appropriate Salience

```drl
rule "Critical Alert" salience 1000
rule "Standard Alert" salience 500
rule "Info Alert" salience 100
```

#### Maintainability Patterns

##### Rule Naming Conventions

```drl
rule "Domain_Condition_Action_Version"
// Examples:
rule "Diabetes_HbA1c_High_Alert_v1"
rule "Medication_Allergy_Interaction_Warning_v2"
```

##### Modular Rule Organization

```
rules/
├── patient-flags/
│   ├── sepsis-detection.drl
│   └── allergy-warnings.drl
├── medication/
│   ├── interaction-checking.drl
│   └── dosage-validation.drl
└── common/
    ├── helper-functions.drl
    └── global-definitions.drl
```

##### Documentation Standards

```drl
/**
 * Detects potential sepsis based on SIRS criteria
 *
 * @author Clinical Team
 * @version 2.0
 * @date 2024-01-15
 * @requirements
 *   - Temperature, Heart Rate, WBC observations
 *   - Observations within 24 hours
 * @triggers Patient flag creation
 */
rule "Sepsis_SIRS_Detection_v2"
```

#### Clinical Safety Patterns

##### Input Validation

```drl
rule "Validate Blood Pressure Reading"
when
    $obs : Obs(concept.uuid == "BP_SYSTOLIC",
               valueNumeric < 50 || valueNumeric > 300)
then
    insert(new ValidationError("Invalid BP reading: " + $obs.getValueNumeric()));
    retract($obs);
end
```

##### Graceful Degradation

```drl
rule "Medication Alert with Fallback"
when
    $patient : Patient()
    $med : Medication()

    // Try to get recent weight
    $weight : Obs(person == $patient, concept.uuid == "WEIGHT",
                  obsDatetime > "24_HOURS_AGO") or

    // Fallback to any weight if recent not available
    $weight : Obs(person == $patient, concept.uuid == "WEIGHT")
then
    // Calculate dose with available weight
    calculateDose($patient, $med, $weight);
end
```

##### Audit Trail Integration

```drl
then
    Alert $alert = new Alert("High fever detected");
    $alert.setSource("Drools Rule: Fever_Detection_v1");
    $alert.setTimestamp(new Date());
    insert($alert);

    // Log for audit trail
    service.logRuleExecution("Fever_Detection_v1", $patient.getUuid(), $alert);
end
```

---

### 15. Session Management and Lifecycle

#### Session Creation Patterns

##### On-Demand Pattern (Default)

```java
// Triggered by API call or event
DroolsSession session = droolsService.createSession("SessionId", params);
session.execute();
Collection<Alert> results = session.getObjects(Alert.class);
session.dispose();
```

##### Auto-Start Pattern

```json
{
  "sessionConfigs": [
    {
      "sessionId": "MonitoringSession",
      "autoStart": true,
      "systemEventListeners": ["org.openmrs.event.EncounterCreatedEvent"]
    }
  ]
}
```

#### Memory Management

##### Fact Lifecycle

1. **Insertion** - Facts added to working memory
2. **Matching** - Pattern matching against rules
3. **Modification** - Fact updates trigger re-evaluation
4. **Retraction** - Facts removed from memory

##### Logical Insertions

```drl
rule "Logical Alert Creation"
when
    $patient : Patient()
    $obs : Obs(person == $patient, valueNumeric > 38.5)
then
    insertLogical(new FeverAlert($patient));
end

// Alert automatically retracted when supporting facts change
```

##### Memory Cleanup Strategies

- Regular fact retraction
- Session disposal
- Logical insertion cleanup
- Garbage collection optimization

#### Session State Management

##### Stateful Session Benefits

- Fact persistence between rule firings
- Complex event processing
- Multi-step workflows
- Incremental updates

##### Stateful Session Challenges

- Memory management complexity
- Thread safety considerations
- State synchronization
- Error recovery

---

### 16. Event-Driven Rules and Listeners

#### OpenMRS Event Integration

##### System Event Listeners

```json
{
  "sessionConfigs": [
    {
      "sessionId": "EventDrivenSession",
      "systemEventListeners": [
        "org.openmrs.event.PatientCreatedEvent",
        "org.openmrs.event.EncounterCreatedEvent",
        "org.openmrs.event.ObsCreatedEvent"
      ]
    }
  ]
}
```

##### Event Handler Implementation

```java
public class DroolsSystemEventListener implements EventListener {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof EncounterCreatedEvent) {
            EncounterCreatedEvent encounterEvent = (EncounterCreatedEvent) event;

            // Execute rules for new encounter
            Map<String, Object> params = new HashMap<>();
            params.put("encounter", encounterEvent.getEncounter().getUuid());

            droolsService.executeSession("EncounterRules", params);
        }
    }
}
```

#### Runtime Event Listeners

##### Fact Insertion Events

```java
public class RuleRuntimeEventListener implements org.drools.core.event.RuleRuntimeEventListener {

    @Override
    public void objectInserted(ObjectInsertedEvent event) {
        Object fact = event.getObject();
        logger.info("Fact inserted: " + fact.getClass().getSimpleName());

        // Custom logic for fact insertion
        if (fact instanceof Alert) {
            notificationService.sendAlert((Alert) fact);
        }
    }

    @Override
    public void objectUpdated(ObjectUpdatedEvent event) {
        // Handle fact modifications
    }

    @Override
    public void objectDeleted(ObjectDeletedEvent event) {
        // Handle fact retractions
    }
}
```

#### Real-Time Processing Patterns

##### Continuous Monitoring

```drl
rule "Continuous Vital Signs Monitoring"
    agenda-group "monitoring"
when
    $patient : Patient()
    $vitals : VitalSigns(patientId == $patient.uuid) from entry-point "VitalSignsStream"

    // Check for critical values
    eval($vitals.isCritical())
then
    insert(new CriticalAlert($patient, $vitals));
end
```

##### Sliding Window Analysis

```drl
rule "Blood Pressure Trend Analysis"
when
    $patient : Patient()

    // Collect BP readings from last 3 visits
    $readings : List() from collect(
        Obs(person == $patient,
            concept.uuid == "BP_SYSTOLIC",
            obsDatetime > "3_VISITS_AGO")
    )

    eval(calculateTrend($readings) == "INCREASING")
then
    insert(new TrendAlert($patient, "BP increasing over 3 visits"));
end
```

---

### 17. Agenda Management and Control

#### Agenda Groups

##### Purpose and Benefits

- **Rule Organization** - Group related rules
- **Execution Control** - Control rule firing order
- **Performance** - Execute only relevant rules
- **Maintenance** - Easier rule management

##### Configuration Example

```json
{
  "sessionConfigs": [
    {
      "sessionId": "ClinicalRules",
      "agendaGroup": "patient-safety"
    }
  ]
}
```

##### DRL Agenda Group Usage

```drl
rule "Drug Allergy Check"
    agenda-group "patient-safety"
    salience 1000
when
    // High priority safety rule
then
    // Safety check logic
end

rule "Preventive Care Reminder"
    agenda-group "quality-measures"
    salience 100
when
    // Lower priority quality rule
then
    // Quality measure logic
end
```

#### Agenda Filters

##### Custom Filter Implementation

```java
public class CriticalAlertsFilter implements AgendaFilter {

    @Override
    public boolean accept(Activation activation) {
        Rule rule = activation.getRule();

        // Only execute critical priority rules
        return rule.getName().contains("Critical") ||
               rule.getName().contains("Emergency");
    }
}
```

##### Filter Registration

```java
public class CriticalRulesProvider implements RuleProvider {

    @Override
    public Collection<SessionConfig> getSessionConfigs() {
        SessionConfig config = new SessionConfig();
        config.setSessionId("CriticalOnly");
        config.setAgendaFilter(new CriticalAlertsFilter());

        return Collections.singletonList(config);
    }
}
```

#### Focus and Auto-Focus

##### Manual Focus Control

```drl
rule "Set Focus on Emergency"
when
    $emergency : EmergencyEvent()
then
    drools.getKnowledgeRuntime().getAgenda().getAgendaGroup("emergency").setFocus();
end
```

##### Auto-Focus Groups

```drl
rule "Emergency Response"
    agenda-group "emergency"
    auto-focus true
when
    $patient : Patient()
    $vitals : VitalSigns(patientId == $patient.uuid, isEmergency == true)
then
    insert(new EmergencyAlert($patient, "Critical vital signs"));
end
```

---

### 18. Fact Management - Insert vs Logical Insert

#### Regular Insert

##### Characteristics

- **Permanent** - Fact remains until explicitly retracted
- **Manual Management** - Developer controls lifecycle
- **Performance** - Lower overhead
- **Use Cases** - Persistent data, audit records

##### Example Usage

```drl
rule "Create Audit Record"
when
    $patient : Patient()
    $action : PatientAction()
then
    AuditRecord $audit = new AuditRecord($patient, $action, new Date());
    insert($audit); // Permanent audit record
end
```

#### Logical Insert

##### Characteristics

- **Conditional** - Fact exists only while supporting facts exist
- **Automatic Management** - System controls lifecycle
- **Truth Maintenance** - Automatically maintains consistency
- **Use Cases** - Derived facts, calculated values

##### Example Usage

```drl
rule "Calculate Risk Score"
when
    $patient : Patient()
    $diabetes : Obs(person == $patient, concept.uuid == "DIABETES", valueCoded == "YES")
    $smoking : Obs(person == $patient, concept.uuid == "SMOKING", valueCoded == "YES")
    $age : Obs(person == $patient, concept.uuid == "AGE", valueNumeric > 65)
then
    // Risk score exists only while all conditions are true
    insertLogical(new HighRiskPatient($patient, "Diabetes + Smoking + Age"));
end
```

#### Truth Maintenance System

##### Automatic Retraction

```drl
rule "Fever Alert"
when
    $patient : Patient()
    $temp : Obs(person == $patient, concept.uuid == "TEMPERATURE", valueNumeric > 38.5)
then
    insertLogical(new FeverAlert($patient));
end

// If temperature drops below 38.5, FeverAlert
```
