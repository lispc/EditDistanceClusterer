#pragma once
#include <vector>

template <typename _IDType, typename _SimType>
struct JoinResult
{
	_IDType id1;
	_IDType id2;
	_SimType s;
};

typedef JoinResult<unsigned, double> JaccardJoinResult;
typedef JoinResult<unsigned, unsigned> EDJoinResult;

const int SUCCESS = 0;
const int FAILURE = 1;

class SimJoiner
{
public:
	SimJoiner();
	~SimJoiner();

	int joinJaccard(const char *filename1, const char *filename2, unsigned q, double threshold, std::vector<JaccardJoinResult> &result);
	int joinED(const char *filename1, const char *filename2, unsigned q, unsigned threshold, std::vector<EDJoinResult> &result);
};

