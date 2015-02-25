#include "SimJoiner.h"
#include <fstream>
#include <iostream>
#include <string>
#include <cstdlib>
#include <cstring>
#include <vector>
#include <utility>
#include <unistd.h>
#include <algorithm>
#include <unordered_map>
#include <tuple>
using namespace std;
#define MAX_LEN 1024
int buf[1024][1024];
vector<string> ss;
void init_buf(){
    for(int i=0;i<1024;i++){
        buf[0][i]=i;
        buf[i][0]=i;
    }
}
SimJoiner::SimJoiner()
{
}

SimJoiner::~SimJoiner()
{
}
inline int mymin(int a,int b){
    return a<b?a:b;
}
inline int mymax(int a,int b){
    return a<b?b:a;
}
int SimJoiner::joinJaccard(const char *filename1, const char *filename2, unsigned q, double threshold, vector<JaccardJoinResult> &result)
{
    result.clear();
    return SUCCESS;
}
int ed(string s1,string s2,int threshold){
    if(threshold<0)
        return 0;
    int tao = threshold;
    int l1 = s1.size();
    int l2 = s2.size();
    if(l1==0){
        return l2;
    }
    if(l2==0){
        return l1;
    }
    for(int j=1;j<=l1;j++){
        int st = max(j-tao,1);
        int ed = min(l2,j+tao);
        if(j-tao-1>=1)
            buf[j-tao-1][j]=tao+1;
        for(int i=st;i<=ed;i++){
            if(s1[j-1]==s2[i-1])
                buf[i][j]=buf[i-1][j-1];
            else{
                buf[i][j]=min(buf[i-1][j-1]+1,min(buf[i-1][j]+1,buf[i][j-1]+1));
            }
        }
        if(ed<l2)
            buf[ed+1][j]=tao+1;
        bool b = true;
        for(int i=st;i<=ed;i++){
            if(buf[i][j]<=tao){
                b = false;
                break;
            }
        }
        if(b)
            return tao+1;
    }
    return buf[l2][l1];
}
bool comp(EDJoinResult const & a,EDJoinResult const & b){
    if(a.id1<b.id1)
        return true;
    if(a.id1==b.id1&&a.id2<b.id2)
        return true;
    if(a.id1==b.id1&&a.id2==b.id2&&a.s<b.s)
        return true;
    return false;
}
bool same(EDJoinResult const & a, EDJoinResult const & b){
    return a.id1==b.id1&&a.id2==b.id2;//&&a.s==b.s;
}
int SimJoiner::joinED(const char *filename1, const char *filename2, unsigned q, unsigned threshold, vector<EDJoinResult> &result)
{
    result.clear();
    init_buf();
    //assert file1 and file2 are already sorted
    ifstream if1(filename1);
    ifstream if2(filename2);
    string indexee;
    int tao = threshold;
    int line_id = 0;
    vector<vector<unordered_map<string,vector<int>>>> global_index;
    while(getline(if1,indexee)){
        ss.push_back(indexee);
        auto l = indexee.size();//3 3 2
        while(global_index.size()<=l){
            int c = 0;
            vector<unordered_map<string,vector<int>>> s_index;
            while(c<tao+1){
                unordered_map<string,vector<int>> ss_index;
                s_index.push_back(ss_index);
                c++;
            }
            global_index.push_back(s_index);
        }
        int lb = l/(tao+1);//8/3=2
        int long_num = l-lb*(tao+1);//2=8-2*3
        int start_pos = 0;
        for(int i=0;i<tao+1;i++){
            int len;
            if(i<long_num){
                len = lb+1;
            }else{
                len = lb;
            }
            string seg = indexee.substr(start_pos,len);
            if(global_index[l][i].find(seg)!=global_index[l][i].end()){
                global_index[l][i][seg].push_back(line_id);
            }else{
                vector<int> vi;
                vi.push_back(line_id);
                global_index[l][i][seg]=vi;
            }
            start_pos += len;
        }
        line_id += 1;
    }
    string item;
    int item_id = 0;
    while(getline(if2,item)){
        vector<tuple<int,int,int,int>> local_mid_res;
        int item_len = item.length();
        for(int target_len = mymax(0,item_len-tao);target_len<=mymin(global_index.size()-1,item_len+tao);target_len++){
            for(int tt=0;tt<=tao;tt++){
                int pos = target_len/(tao+1)*tt;
                int ss_len;
                if(tt<target_len%(tao+1)){
                    ss_len=target_len/(tao+1)+1;
                    pos+=tt;
                }else{
                    pos+=target_len%(tao+1);
                    ss_len=target_len/(tao+1);
                }
                int minpos = mymax(pos-tao,0);
                int maxpos = mymin(pos+tao,item_len-ss_len);
                for(;minpos<=maxpos;minpos++){
                    string seg = item.substr(minpos,ss_len);
                    for(int k=0;k<global_index[target_len][tt][seg].size();k++){
                        int id = global_index[target_len][tt][seg][k];
                        local_mid_res.push_back(make_tuple(id,pos,minpos,ss_len));
                    }
                }
            }
        }
        for(auto t:local_mid_res){
            int tid = get<0>(t);
            int tpos = get<1>(t);
            int ipos = get<2>(t);
            int len = get<3>(t);
            string item_l = item.substr(0,ipos);
            string target_l = ss[tid].substr(0,tpos);
            int ed_value = ed(item_l,target_l,tao);
            if(ed_value>tao){
                continue;
            }else{
                int r_tao = tao - ed_value;
                string item_r = item.substr(ipos+len);
                string target_r = ss[tid].substr(tpos+len);
                int r_ed = ed(item_r,target_r,r_tao);
                if(r_ed>r_tao){
                    continue;
                }else{
                    EDJoinResult r;
                    r.id1 = tid;
                    r.id2 = item_id;
                    r.s = ed_value+r_ed;
                    result.push_back(r);
                }
            }
            
        }
        item_id++;
    }
    sort(result.begin(),result.end(),comp);
    result.erase( unique( result.begin(), result.end(), same), result.end() );
    return SUCCESS;
}

