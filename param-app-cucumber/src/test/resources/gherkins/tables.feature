Feature: The managed parameters are specified by table in the dictionary
  
  Scenario: A parameter table edit page is available to all users
    Given from the home page
    When the user access to list of parameter tables page
    Then the provided template is parameter table list
    
  Scenario: Adding a new parameter table lists the available tables from managed database
    Given a managed database with two tables
    When the user access to new parameter table page
    Then the existing tables are displayed
    
  Scenario: A parameter table can be initialized from the list of available tables
    Given a managed database with two tables
    And the user is on new parameter table page
    When the user select one table to create
    Then the provided template is parameter table create
    And the selected table data are initialized
    And the default domain is automatically selected
    
  Scenario: An initialized parameter table is added to the project dictionary
    Given a prepared parameter table data with name "My Table" for managed table "TTAB_ONE"
    When the parameter table is saved by user
    Then the parameter table is added to the current user's project dictionary
    And the provided template is parameter table list
    
  Scenario: A dictionary entry table content is available for testing. The result is provided from stale table details
    Given a prepared parameter table data with name "My Table" for managed table "TTAB_TWO"
    When the parameter table is tested by user
    Then the parameter table query result is provided with 10 detailled lines from managed table "TTAB_TWO"
    
    