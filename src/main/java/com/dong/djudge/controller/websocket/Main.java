import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner input  =new Scanner(System.in);
        int L=input.nextInt();
        System.out.println(L);
        int[] a =new int[L+1];
        System.out.println(Arrays.toString(a));
        int count=0;
        for(int i=0;i<=L;i++) //将路上的所有树打上标记，表示这个点没有被访问过
        {
            a[i]=0;
        }
        int M=input.nextInt();
        for(int i=1;i<=M;i++)   //循环M次
        {
            int start=input.nextInt(); //区间的头
            int end=input.nextInt();   //区间的尾
            for(int j=start;j<=end;j++)
            {
                if(a[j]==0)
                {
                    a[j]=1;
                }
            }
        }
        for(int i=0;i<=L;i++)
        {
            if(a[i]==0)
            {
                count++;
            }
        }
        System.out.println(count);
    }
}