# sed -i -f noopener.sed *.html
/rel="noopener"/ ! s/target="_blank"/target="_blank" rel="noopener"/g
