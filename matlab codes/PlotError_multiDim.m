clear;
close all;
% add folder and its subfolders to the search paths.
%addpath(genpath('c:/matlab/myfiles'));
% add a folder to the seraching paths.
addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/2DWorld');
% addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/2DWorld/wind1,vel1');

dim =2;
frame= 0.003;
plotLength = 1990;
plotEnd =4000;
filenames={'usp.txt','pred.txt','input.txt','target.txt'};
if(dim==3)
    file_ID=fopen('usp.txt');
    usp = textscan(file_ID,'[%f %f %f]','Delimiter',',');
    file_ID=fopen('pred.txt');
    pred =textscan(file_ID,'[%f %f %f]','Delimiter',',');
    file_ID=fopen('input.txt');
    input =textscan(file_ID,'[%f %f %f]','Delimiter',',');
    file_ID=fopen('target.txt');
    target =textscan(file_ID,'[%f %f %f]','Delimiter',',');
elseif dim==2
     file_ID=fopen('usp.txt');
    usp = textscan(file_ID,'[%f %f]','Delimiter',',');
    file_ID=fopen('pred.txt');
    pred =textscan(file_ID,'[%f %f]','Delimiter',',');
    file_ID=fopen('input.txt');
    input =textscan(file_ID,'[%f %f]','Delimiter',',');
    file_ID=fopen('target.txt');
    target =textscan(file_ID,'[%f %f]','Delimiter',',');
elseif dim ==1
    file_ID=fopen('usp.txt');
    usp = textscan(file_ID,'[%f]','Delimiter',',');
    file_ID=fopen('pred.txt');
    pred =textscan(file_ID,'[%f]','Delimiter',',');
    file_ID=fopen('input.txt');
    input =textscan(file_ID,'[%f]','Delimiter',',');
    file_ID=fopen('target.txt');
    target =textscan(file_ID,'[%f]','Delimiter',','); 
  
end
    file_ID=fopen('err.txt');
    err =textscan(file_ID,'%f','Delimiter',',');
    file_ID=fopen('errvar.txt');
    errvar =textscan(file_ID,'%f','Delimiter',',');
    

figure()


x = 1:1:length(target{1});
% plot(x,target{1},'bo',x,pred{1},'r-');
if dim == 2
plot(target{1}(plotEnd-plotLength+1:plotEnd),target{2}(plotEnd-plotLength+1:plotEnd),'bo',pred{1}(plotEnd-plotLength+1:plotEnd),pred{2}(plotEnd-plotLength+1:plotEnd),'r*');
% plot(target{1}(startplotTime:end),target{2}(startplotTime:end),'bo',pred{1}(startplotTime:end),pred{2}(startplotTime:end),'r-*',usp{1},usp{2},'g-*');

elseif dim == 3
% plot3(target{1}(1900:end),target{2}(1900:end),target{3}(1900:end),'bo',pred{1}(1900:end),pred{2}(1900:end),pred{3}(1900:end),'r-o',usp{1},usp{2},usp{3},'g*-');
plot3(target{1}(startplotTime:end),target{2}(startplotTime:end),target{3}(startplotTime:end),'bo',pred{1}(startplotTime:end),pred{2}(startplotTime:end),pred{3}(startplotTime:end),'r-o');

end
legend('true','predict')

% legend('true','predict','unspervised')
% xlabel('x1');
% ylabel('x2');
xlim([-frame,frame]);
ylim([-frame,frame]);
% zlabel('x3')
% figure()
% u=911:1:920;
% predict_Inverse= pred(u);
% output_Inverse =input(u);
% target_Inverse = target(u);
% plot(u,predict_Inverse,'ro',u,output_Inverse,'co',u,target_Inverse,'b-o');
% title('The Inverse prediction')
% legend('predict','current sensation','target sensation');








figure()
x = 1:1:length(err{1});
axis tight;
plot(x,err{1},'r.')
ylim([0,0.005]);
title('Error')


figure()
shadedErrorBar(x,err{1},sqrt(errvar{1}),'r.',0.6);
title('Error with variance')
ylim([0,0.05]);



