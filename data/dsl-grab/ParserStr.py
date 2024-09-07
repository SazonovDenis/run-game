from Parser import ParserBase


class ParserStr(ParserBase):

    def handle(self, token, tokenType):
        if token == "\n":
            #
            self.onToken("", "new-line")

        elif token[0:1] != " " and token[0:1] != "\t":
            # Строка началась не с пробела или табуляции - это началось новое слово
            self.onToken("", "new-line")
            # Саму строку перебираем посимвольно
            for s in token:
                if s == "\n":
                    self.onToken("", "end-line")
                else:
                    self.onToken(s, "text")
        else:
            # Перебираем посимвольно
            for s in token:
                if s == "\n":
                    self.onToken("", "end-line")
                else:
                    self.onToken(s, "text")
