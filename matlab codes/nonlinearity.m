%{
 This code measures the non-linearity of the developed gate
for lstm 1-1-1
%}


%% constructing state matrix 'matrix'
clear
close all
addpath('/Users/pohsuanhuang/Desktop/Curriculum Section/2016SS/Butz Lab/Java Code/GateDetect1D/final weights analysis');
file_ID=fopen('input.txt');
input = textscan(file_ID,'%f');
file_ID=fopen('target.txt');
target = textscan(file_ID,'%f');
file_ID=fopen('gatehist.txt');
hist = textscan(file_ID,'%f');
target= target{1}';
input = input{1}';
dataSize =size(hist{1},1);
trunc  = 100; % the length of the duration of listening.
neurons_num = 15 ; % for LSTM 1-1-1
% neurons_num = 28 ; % for LSTM 1-2-1
steps_num = dataSize/neurons_num;
matrix = reshape(hist{1},neurons_num,steps_num); % (hist{1},15,100) for LSTM 1-1-1 

target_trunc = target(1,end-trunc+1:end);
input_trunc = input(1,end-trunc+1:end);
%% Plot the gate, predict, and target
close all
show = true;
if show
    figure(1)
    plot(1:trunc,target_trunc(end-trunc+1:end),'.r',1:trunc,matrix(12,end-trunc+1:end),'v');
    ax1 = gca;

    %create xticklabels
    labels =cell(1, trunc); 
    for s = 1:length(input_trunc)
        labels{s}= num2str(round(input_trunc(s)*10)/10); 
    end
    ax1.XTick = 0:1:trunc;
    ax1.XTickLabelRotation = 100;
    set(gca,'xTickLabels', labels);
    hold on
    plot(1:trunc,matrix(neurons_num,end-trunc+1:end),'s');
    hold off
    l = legend ('target','gate','predict','Location','SouthEast');
    l.FontSize = 14;
    xlabel('location','FontSize',20)
    ylabel('velocity','FontSize',20)
    RMSE = sqrt(mean((target_trunc(end-trunc+1:end)-matrix(12,end-trunc+1:end)).^2,2 ));
    sc = sprintf('weight 2 \n error : %f', RMSE);
    title(sc,'FontSize',20 );
end
%% sigmoid fitting
boundary = zeros(1,trunc);
for i = 1:trunc-1
    boundary(i) = sign(target_trunc(i))*sign(target_trunc(i+1));
end
data = matrix(12,end-trunc+1:end); %gate values

% Range of fitting base : 3
base = 4; 
baseSize = 2*base +1;
id = boundary == -1; 
idx = zeros(length(id),2*base+1);
idx(:,1) = id;
xa1 = zeros(1,2*base+1);
xa2 = zeros(1,2*base+1);

vel = 0.2 ; 
bound = 0.8;
xa1(base+1) = bound;
xa2(base+1) = -bound;

for n = 1:base
    xa1(base+1+n) = xa1(base+1) + vel*n;
    xa1(base+1 -n) = xa1(base+1) - vel*n;
    xa2(base+1+n) = xa2(base+1) + vel*n;
    xa2(base+1 -n) = xa2(base+1) - vel*n;
    idx(:,2*n) = circshift(id,[0,-n]);
    idx(:,2*n+1) = circshift(id,[0,n]);
end

IDX = logical(sum(idx,2));
data2 = data(IDX); % gate values as triples at boundary.
data3 =  reshape(data2, baseSize,length(data2)/baseSize); % each column represents a set. 

dataAscend = data3(:,sign(data3(1,:))==1);    %for acending bondaries 
dataDescend = data3(:,sign(data3(1,:))==-1);    %for descending bondaries 
positions = input_trunc(IDX);
positions2 =  reshape(positions, baseSize,length(positions)/baseSize); % each column represents a triple. 
posAscend = positions2(:,sign(positions2(1,:))==1);
posDescend = positions2(:,sign(positions2(1,:))==-1);    %for descending bondaries 


%% fitting sigmoid function

lb1 = [-1 , -100 -1 ];
ub1 = [1 , 100 1];
lb2 = [-1 -100 -1];
ub2 = [1 100 1];

fitter = @(A,x)(A(1)./(1+exp(A(2).*(x-A(3)))))-A(4)  ;

for i = 1: length(data3(1,:))/2
    y1 = dataAscend(:,i)';
    A(:,i) = lsqcurvefit(fitter ,[0 1 0.8 0],xa1,y1);

    y2 = dataDescend(:,i)';
    B(:,i) = lsqcurvefit(fitter,[0 -1 -0.8 0],xa2,y2);
end

%% take mean of the coefficients, and calculate the variances.
A_mean = mean(A,2); 
B_mean = mean(B,2); 
A_var = var(A,1,2);
B_var = var(B,1,2);



figure(2)
b = 1:1:baseSize;

subplot(2,1,1)
hold on
%plot fitting curve
x = posAscend(:,1);
plot(b,fitter(A_mean,xa1),'--');

% plot all data
for i = 1:length(posAscend(1,:))
    plot(b,dataAscend(:,i),'o');
end

hold off
xlabel('location','FontSize',20);
ylabel('gate value','FontSize',20);
ax1 =gca;
%create xticklabels
labels= num2str(round(posAscend(:,1)*100)/100); 
ax1.XTick = 1:1:length(posAscend(:,1));
set(ax1,'xTickLabels', labels);
sc = sprintf('sigmoid fit nonlinearity : %f \n variance : %f',A_mean(2),A_var(2) );
title(sc,'FontSize',20)

subplot(2,1,2)
hold on
% plot fitting curve
plot (b,fitter(B_mean,xa2),'--');

% plot all data
for i = 1:length(posDescend(1,:))
    plot(b,dataDescend(:,i),'o');
end
hold off
xlabel('location','FontSize',20);
ylabel('gate value','FontSize',20);
ax2 =gca;
%create xticklabels
labels= num2str(round(posDescend(:,1)*100)/100); 
ax2.XTick = 1:1:length(posDescend(:,1));
set(ax2,'xTickLabels', labels);

sc = sprintf('sigmoid fit nonlinearity  : %f \n variance : %f',B_mean(2),B_var(2) );
title(sc,'FontSize',20)

%%


a  = ([A_mean(2) A_var(2) A_mean(1) A_var(1) B_mean(2) B_var(2) B_mean(1) B_var(1)])

