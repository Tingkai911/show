Admin Commands - need to enter password:

- setup \<show number> \<number of rows> \<number of seats per row> \<cancellation window in minutes>
    - example: setup 1 10 10 2
    - returns a success message if the setup is successful
- view \<show number>
    - example: view 1
    - returns the show number, ticket number, buyer phone number and seat number

Buyer Commands - no password needed:

- availability \<show number>
    - example: availability 1
    - return a list of available seats
- book \<show number> \<phone number> \<seat number>(,\<seat number>)
    - example: book 1 91234567 A1
    - returns the show number, ticket number, buyer phone number and seat number that is successfully booked
- cancel \<ticket number> \<phone number>
    - example: cancel 123 91234567
    - returns the ticket number that is successfully canceled

Constrains:

- Assume max seats per row is 10 and max rows are 26. Example seat number A1, H5 etc. The “Add” command for admin must
  ensure rows cannot be added beyond the upper limit of 26.
- After booking, User can cancel the seats within a time window of 2 minutes (configurable). Cancellation after that is
  not allowed.
- Only one booking per phone# is allowed per show.

Assumptions:

1) Standalone application, no common data storage for shows. Suppose there are multiple containers, shows in one
   container is not visible in the other.
2) In-memory data used, means that data is not required to be persistent. As such there is no mechanism for data backup
   and restore when the application exits or restarts.
3) Standalone application that is accessible only by the terminal, concurrent is not required as only one thread is
   processing a request at one time. That being said, there are some measures in the code to deal with thread safe
   issues.
4) Admin user is able to run commands for the Buyer user as well
5) Since everything is in-memory, the verification of admin user is based on the password hardcoded in application.yml.
   This is clearly not a secure way to verify admin users.

To build the jar file and run the executable:

1) mvn clean package
2) navigate to "target" folder
3) java -jar show-1.jar

To run and check unit test coverage:

1) mvn clean test
2) navigate to "target" folder
3) navigate to "jacoco-ut" and open index.html in chrome

![Design](https://raw.githubusercontent.com/Tingkai911/show/main/jpm.drawio.png)
