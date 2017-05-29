function v = sigmoid(d)
[rows, cols] = size(d);
v = zeros(rows, cols);

for i = 1:rows
    for j = 1:cols
        v(i,j) = 1/(1+exp(-d(i,j)));
    end
end
