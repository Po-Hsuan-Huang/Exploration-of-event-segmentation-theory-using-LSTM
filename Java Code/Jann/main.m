clear;
% add folder and its subfolders to the search paths.
%addpath(genpath('c:/matlab/myfiles'));
% add a folder to the seraching paths.
addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/Butz Lab/Java Code');

pred = load('pred.txt');
target = load('target.txt');
err = load('err.txt');
errvar = load('errvar.txt');

figure(1)

x = 1:1:length(target);
plot(x,target,'b',x,pred,'r');

xlabel('timestep');
ylabel('loc');


figure(2)
x = 1:1:length(err);
axis tight;
plot(x,err)
title('Error')


figure(3)
shadedErrorBar(x,err,sqrt(errvar),'-ro',0.6);
title('Error with ErrorBar')



