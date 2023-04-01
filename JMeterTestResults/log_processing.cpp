#include <iostream>
#include <string>
#include <sstream>
#include <string.h>
#include <fstream>
#include <vector>
typedef long long ll;

using namespace std;

int main(int argc, char **argv)
{
    string file_path = argv[1];
    fstream log_file;
    log_file.open(file_path, ios::in);
    if (!log_file.is_open())
    {
        cout << "Error: file open failed" << endl;
        return 1;
    }

    int row_cnt = 0;
    string line, tmp, element;
    double ts_sum = 0.0, tj_sum = 0.0;
    getline(log_file, line);
    vector<string> word_list;
    while (log_file >> tmp && tmp != "")
    {
        word_list.clear();

        stringstream s(tmp);

        while (getline(s, element, ','))
        {
            word_list.push_back(element);
        }

        ll ts = stoll(word_list[0]);
        ll tj = stoll(word_list[1]);
        ts_sum += (double)ts / 1e6;
        tj_sum += (double)tj / 1e6;

        ++row_cnt;
    }

    printf("Average TS = %lfms, ", ts_sum / row_cnt);
    printf("Average TJ = %lfms\n", tj_sum / row_cnt); 
    return 0;
}