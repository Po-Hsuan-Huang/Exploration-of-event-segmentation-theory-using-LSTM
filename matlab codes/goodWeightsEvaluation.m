%{
    Running 100 good initial weights trails and observe if the
    errors are really smaller than random initialization.

    The evolved final weights of the two groups are sorted and
    the patterns of weights are plotted
    

%}
%% Preselected initial weights.
%{
 plot histogram of error distribution of final weights that stemed form
 preselected initial weights.
%}

clear
close all
restoredefaultpath;
% add this path to use function ShadedErrorBar.m
addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/matlab code');
% addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/GateDetect1D/good weights analysis');
addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/GateDetect1D/random initial weights [-1 1]/trial3');

file_ID=fopen('weights_in.txt');
weights_in = textscan(file_ID,'%f');
file_ID=fopen('weights_out.txt');
weights_out = textscan(file_ID,'%f');
file_ID=fopen('err.txt');
errors = textscan(file_ID,'%f');
cellnum = 12;
samplenum=1000;
W_in = reshape(weights_in{1},cellnum,samplenum);
W_out = reshape(weights_out{1},cellnum,samplenum);
errors = reshape(errors{1},1,samplenum);

i = 1; % control whitch weight_in to plot.
figure(1);
nbins = 10;
histogram(W_in(i,:),nbins);
BinWidth  = 0.001;
title('weight idx 1 ')

% Histogram of error distribution of preselected initial weights
figure(2)
h1 = histogram(errors);
h1.BinLimits = [0.038,0.046];
h1.BinWidth = 0.001;
title('Error distribution of preselected weights ')
%% Random Initial weights
%{
    plot histogram of error distribution of final weights that stemed form
    random initial weights.
%}
addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/GateDetect1D/random initial weights [-10 10]/trial2');
file_ID=fopen('weights_in.txt');
weights_in = textscan(file_ID,'%f');
file_ID=fopen('weights_out.txt');
weights_out = textscan(file_ID,'%f');
file_ID=fopen('err.txt');
errors1 = textscan(file_ID,'%f');
cellnum = 12;
samplenum=1000;
W_in1 = reshape(weights_in{1},cellnum,samplenum);
W_out1 = reshape(weights_out{1},cellnum,samplenum);
errors1 = reshape(errors1{1},1,samplenum);

i = 10; % control whitch weight to plot.
figure(1);
nbins = 10;
histogram(W_in1(i,:),nbins);
h2 = histogram(errors1,nbins);
h2.BinLimits = [0.038,0.046];
h2.BinWidth = 0.001;
title('Error distribution of random weights of range 10')
%% comparison of errors by sorting 
figure(3)
x = 1:length(errors);

plot(x,sort(errors1),x,sort(errors));
title('sorted error of different weight initailization')
legend('random','preselected');
%%  

bestbase = 100;

[A ,idx]=sort(errors1);
SortW1 = W_out1(:,idx);  %random sampled final weights
[B,idx]= sort(errors);
SortW = W_out(:,idx);    % preselected final weights
x = 1:12;
best_Rnd = SortW1(1:12,1:bestbase);
best_Slc = SortW(1:12,1:bestbase);


% produce the mean of the best weights of preselected weights
signOfBest = sign(best_Slc);
count2 = zeros(bestbase,bestbase);

% element-wise square of the best selected wieghts.
best_Slc_sq = zeros(12,bestbase);
best_Slc_var = zeros(12,bestbase);
for k = 1:bestbase
    count2(k,k)=1;
    best_Slc_sq(:,k) = best_Slc(:,k).^2;
    for i = k+1:bestbase
            % if all the signs equals to the kth weight set
         if sum(count2(:,i)==1)==0 && sum(signOfBest(:,k).*signOfBest(:,i))==12 
             best_Slc(:,k) = best_Slc(:,k) + best_Slc(:,i);
             best_Slc_sq(:,k) = best_Slc(:,k)+best_Slc(:,i).^2;
             count2(k,i) = 1 ;
         end

    end
    % calculate the mean of that class
    best_Slc(:,k)= best_Slc(:,k)/sum(count2(k,:));
    % calcualtate the variance of each wieght
    best_Slc_sq(:,k)= best_Slc_sq(:,k)/sum(count2(k,:));
    best_Slc_var(:,k) = best_Slc_sq(:,k)- best_Slc(:,k).^2 ;

    
end
label2 = sum(count2,2);
label2 = label2(label2~=1);
best_Slc = best_Slc(:,sum(count2,2)~=1);
best_Slc_var= best_Slc_var(:,sum(count2,2)~=1);


% produce the mean of the best weights of random weights
signOfBest = sign(best_Rnd);
count1 = zeros(bestbase,bestbase);

% element-wise square of the best selected wieghts.
best_Rnd_sq = zeros(12,bestbase);
best_Rnd_var = zeros(12,bestbase);
for k = 1:bestbase
    count1(k,k)=1;
    best_Rnd_sq(:,k) = best_Rnd(:,k).^2;

    for i = k+1:bestbase

         if sum(count1(:,i)==1)==0 && sum(signOfBest(:,k).*signOfBest(:,i))==12
             best_Rnd(:,k) = best_Rnd(:,k) + best_Rnd(:,i);
             best_Rnd_sq(:,k) = best_Rnd(:,k)+best_Rnd(:,i).^2;
             count1(k,i) = 1 ;
         end

    end
    
    best_Rnd(:,k)= best_Rnd(:,k)/sum(count1(k,:));
    % calcualtate the variance of each wieght
    best_Rnd_sq(:,k)= best_Rnd_sq(:,k)/sum(count1(k,:));
    best_Rnd_var(:,k) = best_Rnd_sq(:,k)- best_Rnd(:,k).^2 ;
    
end
label = sum(count1,2);
label = label((sum(count1,2)~=1));
best_Rnd = best_Rnd(:,sum(count1,2)~=1);
best_Rnd_var= best_Rnd_var(:,sum(count1,2)~=1);

x  = 1:12;
figure(4)
for idx = 1: size(best_Rnd,2);
subplot(1,size(best_Rnd,2),idx)
% plot(x,best_Rnd(:,idx));
shadedErrorBar(x,best_Rnd(:,idx),sqrt(abs(best_Rnd_var(:,idx))));
scan = sprintf('counts : %d' , label(idx));
title(scan);
end
sc = sprintf('weight patterns of random generatrd best weights of randomization range 10 \n variance in gray shade. ');
suptitle(sc);

figure(5)
for idx = 1: size(best_Slc,2);
subplot(1,size(best_Slc,2),idx)
% plot(x,best_Slc(:,idx));
shadedErrorBar(x,best_Slc(:,idx),sqrt(abs(best_Slc_var(:,idx))));
scan2 = sprintf('counts : %d' ,label2(idx));
title(scan2)
end
sc = sprintf('weight patterns of random best weights of randomization range 1 \n variance in gray shade.');
suptitle(sc);
