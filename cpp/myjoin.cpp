#include <iostream>
#include <cstdlib>
#include "SimJoiner.h"
using namespace std;
int main(int argc, char** argv) {
    SimJoiner joiner;
    vector<EDJoinResult> results;
    joiner.joinED(argv[2], argv[2], 0, atoi(argv[1]), results);
    for (auto item : results) {
        if (item.id2 > item.id1) {
            cout << item.id1 + 1 << " " << item.id2 + 1 << "\n";
        }
    }
}

