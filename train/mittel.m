function a = mittel(b, cnt)

c = ones(cnt,1) ./ cnt;

a = filter(c, 1, b);