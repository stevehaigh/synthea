package org.mitre.synthea.world.concepts;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mitre.synthea.helpers.Config;
import org.mitre.synthea.world.agents.Payer;
import org.mitre.synthea.world.agents.Person;
import org.mitre.synthea.world.agents.Provider;
import org.mitre.synthea.world.concepts.HealthRecord.Code;
import org.mitre.synthea.world.concepts.HealthRecord.Encounter;
import org.mitre.synthea.world.concepts.HealthRecord.EncounterType;
import org.mitre.synthea.world.geography.Location;

public class UncoveredHealthRecordTest {

  Payer testPrivatePayer1;
  Payer testPrivatePayer2;
  Payer noInsurance;

  long time;

  /**
   * Setup for HealthRecord Tests.
   */
  @Before
  public void setup() {
    // Clear any Payers that may have already been statically loaded.
    Payer.clear();
    Config.set("generate.payers.insurance_companies.default_file",
        "generic/payers/test_payers.csv");
    // Load in the .csv list of Payers for MA.
    Payer.loadPayers(new Location("Massachusetts", null));
    // Load the two test payers.
    testPrivatePayer1 = Payer.getPrivatePayers().get(0);
    testPrivatePayer2 = Payer.getPrivatePayers().get(1);

    time = 0L;
  }

  @Test
  public void payerDoesNotCoverEncounter() {

    Person person = new Person(0L);
    person.attributes.put(Person.INCOME, 20000);
    person.setPayerAtAge(0, testPrivatePayer2);
    person.setProvider(EncounterType.WELLNESS, new Provider());
    person.setProvider(EncounterType.AMBULATORY, new Provider());
    Code code = new Code("SNOMED-CT","705129","Fake Code");

    // First encounter is covered.
    Encounter coveredEncounter = person.encounterStart(0L, EncounterType.WELLNESS);
    coveredEncounter.codes.add(code);
    coveredEncounter.provider = new Provider();
    person.record.encounterEnd(0L, EncounterType.WELLNESS);
    assertTrue(person.record.encounters.contains(coveredEncounter));
    assertFalse(person.uncoveredHealthRecord.encounters.contains(coveredEncounter));

    // Second encounter is uncovered.
    Encounter uncoveredEncounter = person.encounterStart(0L, EncounterType.AMBULATORY);
    uncoveredEncounter.codes.add(code);
    uncoveredEncounter.provider = new Provider();
    person.record.encounterEnd(0L, EncounterType.AMBULATORY);
    assertFalse(person.record.encounters.contains(uncoveredEncounter));
    assertTrue(person.uncoveredHealthRecord.encounters.contains(uncoveredEncounter));
  }
}
