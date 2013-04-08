#include<stdio.h>
#include<memory.h>
#include<ctime>
#include<cstdlib>
using namespace std;
struct node
{
    int x,y,size;
} ans[12];
int boxsize[11]= {3,2,2,2,1,1,1,1,1,1,1};
int map[7][4];
void putans()
{
    for (int i=0; i<11; i++)
        printf("%d %d %d\n",ans[i].x,ans[i].y,ans[i].size);
    for (int j=0; j<4; j++)
    {
        for (int i=0; i<7; i++)
            printf("%d",map[i][j]);
        printf("\n");
    }
}
void dfs(int k)
{
    if (k>=11) return;
    if (k==0)
    {
        int temp;
        int x[6]= {0,0,2,2,4,4};
        int y[6]= {0,1,0,1,0,1};
        temp=rand()%6;
        ans[k].x=x[temp];
        ans[k].y=y[temp];
        ans[k].size=boxsize[k];
    }
    else if (k==1 || k==2)
    {
        int temp,ok=0;
        int x[8]= {0,0,2,2,3,3,5,5};
        int y[8]= {0,2,0,2,0,2,0,2};
        while (!ok)
        {
            temp=rand()%8;
            if (map[x[temp]][y[temp]]==0 && map[x[temp]][y[temp]+1]==0 &&
                    map[x[temp]+1][y[temp]]==0 && map[x[temp]+1][y[temp]+1]==0)
                ok=1;
        }
        ans[k].x=x[temp];
        ans[k].y=y[temp];
        ans[k].size=boxsize[k];
    }
    else if (k==3)
    {
        int temp,ok=0;
        int x,y;
        while (!ok)
        {
            x=rand()%6;
            y=rand()%3;
            if (map[x][y]==0 && map[x][y+1]==0 &&
                    map[x+1][y]==0 && map[x+1][y+1]==0)
                ok=1;
        }
        ans[k].x=x;
        ans[k].y=y;
        ans[k].size=boxsize[k];
    }
    else if (k>=4)
    {
        int temp;
        int x=0,y=0;
        for (x=0; x<7; x++)
        {
            for (y=0; y<4; y++)
                if (map[x][y]==0) break;
            if (y<4) break;
        }
        ans[k].x=x;
        ans[k].y=y;
        ans[k].size=boxsize[k];
    }
    for (int i=ans[k].x; i<ans[k].x+ans[k].size; i++)
        for (int j=ans[k].y; j<ans[k].y+ans[k].size; j++)
            map[i][j]=ans[k].size;
    dfs(k+1);
}
int main()
{
    srand((unsigned)time(NULL));
    memset(map,0,sizeof(map));
    dfs(0);
    putans();
    return 0;
}
