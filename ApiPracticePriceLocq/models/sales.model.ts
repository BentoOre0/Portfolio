import { AllowNull, AutoIncrement, Column, Model, PrimaryKey, Table } from 'sequelize-typescript';

@Table({ tableName: 'sales', paranoid: true, underscored: true})
export class Sales extends Model {
  @PrimaryKey
  @AutoIncrement
  @Column
  id: bigint;

  @Column
  amount: number;

  @AllowNull(false)
  @Column
  item: string;

}
