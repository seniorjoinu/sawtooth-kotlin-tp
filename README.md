# sawtooth-kotlin-example

### On windows
* `start.bat -r`

### On linux
* to do

### Story
1. Root validator starts, generates keys for itself and the core validator set (at least 4 nodes needed for pbft, 
    because it is 3f+1 consensus)
2. Root validator does all configuration stuff and proposes core validator set to be pbft members
3. Root validator stores core validator set's keys on endorser-service
4. Core validator set starts
5. Each node from core validator set retrieves it's keys from endorser-service
6. Transaction appears, state changes
7. Adhoc-node starts, generates it's keys and stores them on endorser-service
8. Root validator retrieves adhoc keys from endorser and proposes a new set of pbft members with adhoc node in it
9. State changes
10. Transaction processor update appears - new tp's started, old killed (but it doesn't matter)
11. New transaction appears, state changes
12. Adhoc node killed
13. Root validator proposes a new set of pbft members - without adhoc node
15. State changes
16. Transaction appears, state changes