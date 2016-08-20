clear;
close all;
% add folder and its subfolders to the search paths.
%addpath(genpath('c:/matlab/myfiles'));
% add a folder to the seraching paths.
%addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/ScratchBoard');
addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/GateDetect1D');

    pred = load('pred.txt');
    input = load('input.txt');
    target = load('target.txt');
    err = load('err.txt');
    errvar = load('errvar.txt');
    
figure()


x = 1:1:length(target);
% plot(x,target,'b',x,pred,'r',x,input,'c.');
plot(x,target,'b-',x,pred,'r-');
legend('true','predict','data')
xlabel('timestep');
ylabel('loc');

% figure()
% u=911:1:920;
% predict_Inverse= pred(u);
% output_Inverse =input(u);
% target_Inverse = target(u);
% plot(u,predict_Inverse,'ro',u,output_Inverse,'co',u,target_Inverse,'b-o');
% title('The Inverse prediction')
% legend('predict','current sensation','target sensation');








figure()
x = 1:1:length(err);
axis tight;
plot(x,err)
ylim([0,0.4]);
title('Error')


figure()
shadedErrorBar(x,err,sqrt(errvar),'-ro',0.6);
title('Error with variance')
ylim([0,0.2]);




