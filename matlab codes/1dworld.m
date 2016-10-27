clear;
pred = load('pred.txt');
target = load('target.txt');


figure()

x = 1:1:length(target);
plot(x,target,'bo',x,pred,'rx');

xlabel('timestep');
ylabel('loc');
